package info.voidev.lspidea.features.smartenter

import com.intellij.codeInsight.editorActions.smartEnter.SmartEnterProcessor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import info.voidev.lspidea.LspSessionManager

/**
 * A smart enter processor that sends a non-standard command to the language server,
 * configured in the server definition.
 */
class LspSmartEnterProcessor : SmartEnterProcessor() {
    override fun process(project: Project, editor: Editor, psiFile: PsiFile): Boolean {
        val file = psiFile.virtualFile ?: return false

        val session = LspSessionManager.getInstance(project).getForFile(file) ?: return false

        return session.state.serverDef.smartEnterSupport?.performSmartEnter(session, file, editor) ?: false
    }
}
