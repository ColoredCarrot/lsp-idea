package info.voidev.lspidea.lspex

import info.voidev.lspidea.lspex.inlay.InlayHint
import info.voidev.lspidea.lspex.inlay.InlayHintsParams
import info.voidev.lspidea.lspex.joinlines.JoinLinesParams
import info.voidev.lspidea.lspex.moveitem.MoveItemParams
import info.voidev.lspidea.lspex.moveitem.SnippetTextEdit
import org.eclipse.lsp4j.TextDocumentPositionParams
import org.eclipse.lsp4j.TextEdit
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment
import java.util.concurrent.CompletableFuture

@JsonSegment("experimental")
interface ExperimentalService {

    @JsonRequest
    fun inlayHints(params: InlayHintsParams): CompletableFuture<List<InlayHint>> {
        throw UnsupportedOperationException()
    }

    @JsonRequest
    fun moveItem(params: MoveItemParams): CompletableFuture<List<SnippetTextEdit>> {
        throw UnsupportedOperationException()
    }

    @JsonRequest
    fun joinLines(params: JoinLinesParams): CompletableFuture<List<TextEdit>> {
        throw UnsupportedOperationException()
    }

    @JsonRequest
    fun onEnter(params: TextDocumentPositionParams): CompletableFuture<List<SnippetTextEdit>> {
        throw UnsupportedOperationException()
    }

}
