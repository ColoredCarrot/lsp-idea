package info.voidev.lspidea.features.formatting

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import info.voidev.lspidea.LspSessionManager
import info.voidev.lspidea.editor.applyEdits
import info.voidev.lspidea.util.caretLspPosition
import info.voidev.lspidea.util.identifyForLsp
import info.voidev.lspidea.util.joinUnwrapExceptionsCancellable
import org.eclipse.lsp4j.DocumentOnTypeFormattingParams
import org.eclipse.lsp4j.ServerCapabilities

/**
 * Typed handler that sends "textDocument/onTypeFormatting"
 * when the typed character is listed as a trigger character
 * in the server capabilities.
 */
class LspOnTypeFormatting : TypedHandlerDelegate() {

    override fun charTyped(c: Char, project: Project, editor: Editor, file: PsiFile): Result {
        handleCharTyped(c, project, editor, file)
        return super.charTyped(c, project, editor, file)
    }

    private fun handleCharTyped(c: Char, project: Project, editor: Editor, file: PsiFile) {
        val vfile = file.virtualFile ?: return
        val session = LspSessionManager.getInstance(project).getForIfActive(vfile) ?: return

        if (!shouldInvokeFormatting(c, session.state.serverCapabilities)) return

        val edits = session.server.textDocumentService.onTypeFormatting(
            DocumentOnTypeFormattingParams(
                vfile.identifyForLsp(),
                LspFormattingOptionsProvider.get(file),
                editor.caretLspPosition,
                "$c"
            )
        ).joinUnwrapExceptionsCancellable() // TODO specify an extraordinarily small timeout here
            ?.ifEmpty { null }
            ?: return

        editor.document.applyEdits(edits)
    }

    private fun shouldInvokeFormatting(c: Char, serverCaps: ServerCapabilities): Boolean {
        val provider = serverCaps.documentOnTypeFormattingProvider ?: return false
        return provider.firstTriggerCharacter.startsWith(c) ||
            provider.moreTriggerCharacter.any { it.startsWith(c) }
    }
}
