package info.voidev.lspidea.validation

import org.eclipse.lsp4j.ServerInfo

sealed class LspServerValidationResult(val serverInfo: ServerInfo?) {
    class Success(serverInfo: ServerInfo?) : LspServerValidationResult(serverInfo)

    object Canceled : LspServerValidationResult(null)

    class Failure(val exception: Exception, serverInfo: ServerInfo?) : LspServerValidationResult(serverInfo)
}
