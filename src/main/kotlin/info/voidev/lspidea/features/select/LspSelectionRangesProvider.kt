package info.voidev.lspidea.features.select

import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import info.voidev.lspidea.config.LspIdeaConfig
import info.voidev.lspidea.connect.LspSession
import info.voidev.lspidea.util.identifyForLsp
import info.voidev.lspidea.util.joinUnwrapExceptionsCancellable
import info.voidev.lspidea.util.lspRange2range
import info.voidev.lspidea.util.offset2lspPosition
import org.eclipse.lsp4j.SelectionRangeParams
import org.eclipse.lsp4j.ServerCapabilities

object LspSelectionRangesProvider {

    fun getSelectionRanges(document: Document, offset: Int, session: LspSession): List<TextRange>? {

        var limit = LspIdeaConfig.get(session.project).maxSelectionRanges
        if (limit == 0) return null
        if (limit < 0) limit = Int.MAX_VALUE

        if (!mayFetchSelectionRanges(session.state.serverCapabilities)) return null

        var range = session.server.textDocumentService.selectionRange(
            SelectionRangeParams(
                document.identifyForLsp(),
                listOf(document.offset2lspPosition(offset))
            )
        ).joinUnwrapExceptionsCancellable()
            ?.getOrNull(0)

        val result = ArrayList<TextRange>()
        var count = 0
        while (range != null) {
            if (++count > limit) {
                break
            }

            result += document.lspRange2range(range.range)
            range = range.parent
        }

        return result
    }

    private fun mayFetchSelectionRanges(caps: ServerCapabilities): Boolean {
        return caps.selectionRangeProvider?.let { it.left ?: it.isRight } == true
    }
}
