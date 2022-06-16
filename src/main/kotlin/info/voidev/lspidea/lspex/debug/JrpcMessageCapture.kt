package info.voidev.lspidea.lspex.debug

import org.eclipse.lsp4j.jsonrpc.messages.Message

data class JrpcMessageCapture(
    /** null if unknown (this should never be the case) */
    val direction: JrpcMessageDirection?,
    /** null if unknown (this should never be the case) */
    val kind: JrpcMessageKind?,
    /** null for responses */
    val method: String?,
    /** null for notifications */
    val id: String?,
    /** the actual message, or `null` if it wasn't captured */
    val message: Message?,
    val stackTrace: StackTrace?,
    //TODO: Might also capture handling thread
)

enum class JrpcMessageDirection {
    SENT,
    RECEIVED,
}

enum class JrpcMessageKind(val displayName: String) {
    REQUEST("Request"),
    RESPONSE("Response"),
    NOTIFICATION("Notification"),
}
