package info.voidev.lspidea.errors

import com.intellij.openapi.diagnostic.thisLogger
import info.voidev.lspidea.LspIdea
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException

object LspErrors {

    fun mildError(msg: String, ex: Throwable?) {
        if (ex is ResponseErrorException) {
            LspIdea.showResponseError(msg, ex.responseError, TODO())
        }
    }

    private val logger = thisLogger()
}
