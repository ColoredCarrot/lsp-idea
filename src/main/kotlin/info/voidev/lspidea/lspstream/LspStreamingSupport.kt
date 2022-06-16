package info.voidev.lspidea.lspstream

import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer
import info.voidev.lspidea.connect.LspSession
import org.eclipse.lsp4j.ProgressParams
import java.util.concurrent.ConcurrentHashMap

class LspStreamingSupport(private val session: LspSession) : Disposable {

    /////////////////////
    // TODO: Find a LS that actually streams some results
    /////////////////////

    private val consumers = ConcurrentHashMap<String, (Any) -> Unit>()

    init {
        Disposer.register(session, this)
    }

    fun registerStreamConsumer(token: String, consumer: (Any) -> Unit) {
        consumers[token] = consumer
    }

    fun notifyProgress(params: ProgressParams) {
        if (!session.isActive) {
            return
        }

        val token = params.token.left ?: return
        val consumer = consumers[token] ?: return

        if (params.value.isLeft) {
            TODO()
        } else {
            val streamedValue = params.value.right
            consumer(streamedValue)
        }
    }

    override fun dispose() {
        // TODO Should we notify the consumers somehow?
    }
}
