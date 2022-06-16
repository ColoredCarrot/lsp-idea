package info.voidev.lspidea.diagnostics

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import info.voidev.lspidea.LspSessionManager
import info.voidev.lspidea.features.codeaction.LspCodeActionExecutor
import org.eclipse.lsp4j.CodeAction

/**
 * One instance is created per codeAction.
 * The same instance can be assigned to multiple diagnostic annotations as their quickfix.
 */
class LspCodeActionIntentionAction(
    private val codeAction: CodeAction,
) : IntentionAction {

    override fun startInWriteAction() = false // We will start a write command anyhow, if necessary

    override fun getText() = codeAction.title ?: familyName

    override fun getFamilyName() = "Fix via language server"

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (editor == null || file == null) return false

        return codeAction.disabled == null
    }

    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        if (codeAction.disabled != null) {
            thisLogger().error("Tried to invoke disabled code action: $codeAction")
            return
        }

        val session = LspSessionManager.getInstanceIfCreated(project)?.getForIfActive(file.virtualFile) ?: return

        LspCodeActionExecutor.execute(codeAction, session)
    }
}
