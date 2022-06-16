package info.voidev.lspidea.features.joinlines

import com.intellij.codeInsight.editorActions.JoinRawLinesHandlerDelegate
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiFile
import info.voidev.lspidea.LspSessionManager
import info.voidev.lspidea.editor.applyEdits
import info.voidev.lspidea.lspex.joinlines.JoinLinesParams
import info.voidev.lspidea.util.identifyForLsp
import info.voidev.lspidea.util.joinUnwrapExceptionsCancellable
import info.voidev.lspidea.util.offset2lspPosition
import org.eclipse.lsp4j.Range

class LspJoinLinesHandler : JoinRawLinesHandlerDelegate {

    override fun tryJoinLines(document: Document, file: PsiFile, start: Int, end: Int): Int {
        return -1
    }

    override fun tryJoinRawLines(document: Document, file: PsiFile, start: Int, end: Int): Int {
        val project = file.project
        val vfile = file.virtualFile ?: return -1
        val session = LspSessionManager.getInstance(project).getForFile(vfile) ?: return -1

        // This isn't actually the caret position,
        // but it should be close enough
        val pos = document.offset2lspPosition(start)

        val edits = session.server.experimentalService.joinLines(JoinLinesParams(
            vfile.identifyForLsp(),
            listOf(Range(pos, pos))
        )).joinUnwrapExceptionsCancellable()

        if (edits.isEmpty()) return -1

        document.applyEdits(edits)

        // TODO: Unclear where to place caret now. The LSP should clarify this
        //  (once this graduates from experimental)
        return start
    }

}
