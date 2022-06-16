package info.voidev.lspidea.features.enter

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiFile
import com.intellij.util.containers.nullize
import info.voidev.lspidea.LspSessionManager
import info.voidev.lspidea.editor.ResolvedSnippetTextEdit
import info.voidev.lspidea.editor.sortForApplyingToDocument
import info.voidev.lspidea.util.caretLspPosition
import info.voidev.lspidea.util.identifyForLsp
import info.voidev.lspidea.util.joinLsp
import org.eclipse.lsp4j.ServerCapabilities
import org.eclipse.lsp4j.TextDocumentPositionParams

class LspEnterHandler : EnterHandlerDelegateAdapter() {

    override fun preprocessEnter(
        file: PsiFile,
        editor: Editor,
        caretOffset: Ref<Int>,
        caretAdvance: Ref<Int>,
        dataContext: DataContext,
        originalHandler: EditorActionHandler?,
    ): EnterHandlerDelegate.Result {
        val vfile = file.virtualFile
            ?: return EnterHandlerDelegate.Result.Continue
        val session = LspSessionManager.getInstance(file.project).getForFile(vfile)
            ?: return EnterHandlerDelegate.Result.Continue

        if (!supportsOnEnter(session.state.serverCapabilities)) {
            return EnterHandlerDelegate.Result.Continue
        }

        val edits = session.server.experimentalService.onEnter(TextDocumentPositionParams(
            vfile.identifyForLsp(),
            editor.caretLspPosition
        ))
            .joinLsp(session.project, "Could not handle onEnter")
            .nullize()
            ?: return EnterHandlerDelegate.Result.Continue

        val resolvedEdits = edits.mapTo(ArrayList()) {
            ResolvedSnippetTextEdit(it, it.insertTextFormat, session.project, editor)
        }

        resolvedEdits.sortForApplyingToDocument()
        resolvedEdits.forEach { it.applyTo(editor.document) }

        return EnterHandlerDelegate.Result.Stop
    }

    private fun supportsOnEnter(caps: ServerCapabilities): Boolean {
        val exp = caps.experimental as? JsonObject ?: return false
        return (exp["onEnter"] as? JsonPrimitive)?.asBoolean == true
    }

}
