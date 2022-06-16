package info.voidev.lspidea.features.refactor

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.vfs.VirtualFile
import info.voidev.lspidea.connect.LspSession
import info.voidev.lspidea.features.codeaction.LspCodeActionExecutor
import info.voidev.lspidea.util.selectionRange
import org.eclipse.lsp4j.CodeAction

abstract class AbstractCodeActionBasedLspRefactoringHandler(@NlsContexts.DialogTitle refactoringName: String) :
    AbstractLspRefactoringHandler(refactoringName) {

    protected open fun canUseCodeAction(action: CodeAction): Boolean {
        thisLogger().error("Override either findBestCodeAction or canUseCodeAction in $javaClass")
        return false
    }

    protected open fun findBestCodeAction(available: Collection<CodeAction>): CodeAction? {
        return available.firstOrNull { it.disabled == null && canUseCodeAction(it) }
            ?: available.firstOrNull { canUseCodeAction(it) }
    }

    override fun perform(session: LspSession, editor: Editor, file: VirtualFile) {
        val availableActions = session.codeActionManager
            .getAvailableActions(editor.document, editor.selectionModel.selectionRange)

        val theAction = findBestCodeAction(availableActions)
            ?: return showCannotRefactorHint(session.project, editor)

        if (theAction.disabled != null) {
            return showErrorHint(theAction.disabled.reason, editor, session.project)
        }

        LspCodeActionExecutor.execute(theAction, session)
    }

}
