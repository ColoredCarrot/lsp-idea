package info.voidev.lspidea.features.moveitem

import com.intellij.codeInsight.editorActions.moveUpDown.LineRange
import com.intellij.codeInsight.editorActions.moveUpDown.StatementUpDownMover
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.psi.PsiFile
import info.voidev.lspidea.LspSessionManager
import info.voidev.lspidea.dummy.LspDummyPsiFile
import info.voidev.lspidea.editor.ResolvedSnippetTextEdit
import info.voidev.lspidea.editor.sortForApplyingToDocument
import info.voidev.lspidea.lspex.moveitem.Direction
import info.voidev.lspidea.lspex.moveitem.MoveItemParams
import info.voidev.lspidea.util.identifyForLsp
import info.voidev.lspidea.util.joinUnwrapExceptionsCancellable
import info.voidev.lspidea.util.range2lspRange

class LspStatementUpDownMover : StatementUpDownMover() {

    override fun checkAvailable(editor: Editor, file: PsiFile, info: MoveInfo, down: Boolean): Boolean {
        if (file !is LspDummyPsiFile) return false
        if (editor.project == null) return false

        val session = LspSessionManager.getInstance(file.project).getForFile(file.virtualFile) ?: return false

        // TODO somehow check if LS supports the experimental moveItem

        // If we can move, set toMove equal to toMove2 - IntelliJ will not move anything then
        // See com/intellij/codeInsight/editorActions/moveUpDown/MoverWrapper.java:47
        info.toMove = LineRange(1, 1)
        info.toMove2 = info.toMove

        info.indentSource = false
        info.indentTarget = false

        return true
    }

    override fun beforeMove(editor: Editor, info: MoveInfo, down: Boolean) {
        // Perform the actual move in here

        val project = editor.project ?: return
        val file = FileDocumentManager.getInstance().getFile(editor.document) ?: return
        val session = LspSessionManager.getInstance(project).getForFile(file) ?: return

        val edits = session.server.experimentalService.moveItem(
            MoveItemParams(
                file.identifyForLsp(),
                editor.document.range2lspRange(editor.selectionModel.selectionStart, editor.selectionModel.selectionEnd),
                if (down) Direction.Down else Direction.Up
            )
        ).joinUnwrapExceptionsCancellable()

        // TODO: Validate that there is at most one snippet, BUT:
        //  rust-analyzer sends multiple edits with insertTextFormat == Snippet;
        //  it only guarantees that at most one actually contains any tab stops/variables.
        //  This is likely a bug on their part,
        //  but there's no harm in us supporting it.
//        val numEditsWithSnippet = edits.count { it.insertTextFormat == InsertTextFormat.Snippet }
//        if (numEditsWithSnippet > 1) {
//            LspIdea.showError(
//                "\"Move item\" failed",
//                "${session.state.serverInfo.name} sent more than one snippet",
//                project
//            )
//            return
//        }

        val resolvedEdits = edits.mapTo(ArrayList()) {
            ResolvedSnippetTextEdit(it, it.insertTextFormat, project, editor)
        }

        resolvedEdits.sortForApplyingToDocument()
        resolvedEdits.forEach { it.applyTo(editor.document) }
    }
}
