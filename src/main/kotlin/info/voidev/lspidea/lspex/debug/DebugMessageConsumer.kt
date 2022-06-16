package info.voidev.lspidea.lspex.debug

import org.eclipse.lsp4j.jsonrpc.MessageConsumer
import org.eclipse.lsp4j.jsonrpc.messages.IdentifiableMessage
import org.eclipse.lsp4j.jsonrpc.messages.Message
import org.eclipse.lsp4j.jsonrpc.messages.NotificationMessage
import org.eclipse.lsp4j.jsonrpc.messages.RequestMessage
import org.eclipse.lsp4j.jsonrpc.messages.ResponseMessage

class DebugMessageConsumer(
    private val debugger: JrpcDebugger,
    private val downstream: MessageConsumer,
    private val myDirection: JrpcMessageDirection?,
) : MessageConsumer {

    override fun consume(message: Message) {
        val capture = JrpcMessageCapture(
            myDirection,
            getKind(message),
            getMethod(message),
            (message as? IdentifiableMessage)?.id,
            message.takeIf { shouldCaptureMessage(it) },
            StackTrace.captureIf(debugger.captureStackTraces)
        )
        debugger.messageObserver?.observe(capture)

        downstream.consume(message)
    }

    private fun getMethod(message: Message) = when (message) {
        is RequestMessage -> message.method
        is NotificationMessage -> message.method
        else -> null
    }

    private fun getKind(message: Message) = when (message) {
        is RequestMessage -> JrpcMessageKind.REQUEST
        is ResponseMessage -> JrpcMessageKind.RESPONSE
        is NotificationMessage -> JrpcMessageKind.NOTIFICATION
        else -> null
    }

    private fun shouldCaptureMessage(message: Message) =
        // Always capture error messages
        debugger.captureContents || message is ResponseMessage && message.error != null

}
