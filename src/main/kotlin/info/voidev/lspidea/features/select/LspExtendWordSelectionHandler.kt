package info.voidev.lspidea.features.select

import com.intellij.codeInsight.editorActions.ExtendWordSelectionHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import info.voidev.lspidea.LspSessionManager
import info.voidev.lspidea.dummy.LspElementTypes

class LspExtendWordSelectionHandler : ExtendWordSelectionHandler {

    override fun canSelect(e: PsiElement): Boolean {
        // For some reason, when expanding the selection,
        // we don't get instances of LspDummyPsi*
        val elementType = e.elementType
        return elementType == LspElementTypes.Content || elementType == LspElementTypes.File
    }

    override fun select(
        e: PsiElement,
        editorText: CharSequence,
        cursorOffset: Int,
        editor: Editor,
    ): List<TextRange>? {
        val vfile = e.containingFile?.virtualFile ?: return null
        val session = LspSessionManager.getInstance(e.project).getForFile(vfile) ?: return null

        return LspSelectionRangesProvider.getSelectionRanges(editor.document, cursorOffset, session)

        /*var limit = LspIdeaConfig.get(e.project).maxSelectionRanges
        if (limit == 0) return null
        if (limit < 0) limit = Int.MAX_VALUE

        if (!mayFetchSelectionRanges(session.state.serverCapabilities)) return null

        var range = session.server.textDocumentService.selectionRange(SelectionRangeParams(
            vfile.identifyForLsp(),
            listOf(editor.document.offset2lspPosition(cursorOffset))
        )).joinUnwrapExceptionsCancellable()
            ?.getOrNull(0)

        val result = ArrayList<TextRange>()
        var count = 0
        while (range != null) {
            if (++count > limit) {
                break
            }

            result += editor.document.lspRange2range(range.range)
            range = range.parent
        }

        return result*/
    }

}
