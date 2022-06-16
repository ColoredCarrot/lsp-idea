package info.voidev.lspidea.lspex

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.eclipse.lsp4j.jsonrpc.services.JsonDelegate
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest
import org.eclipse.lsp4j.services.LanguageServer
import java.util.concurrent.CompletableFuture

interface LanguageServerEx : LanguageServer {

    @get:JsonDelegate
    val experimentalService: ExperimentalService
        get() {
            throw UnsupportedOperationException()
        }

    @JsonRequest
    fun fakeRequest(params: JsonObject): CompletableFuture<JsonElement> {
        throw UnsupportedOperationException()
    }

    @JsonNotification
    fun fakeNotification(params: JsonObject) {
        throw UnsupportedOperationException()
    }
}
