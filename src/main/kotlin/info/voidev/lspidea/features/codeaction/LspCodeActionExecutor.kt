package info.voidev.lspidea.features.codeaction

import com.intellij.openapi.diagnostic.thisLogger
import info.voidev.lspidea.command.LspCommandExecutionUtil
import info.voidev.lspidea.connect.LspSession
import info.voidev.lspidea.editor.applyWorkspaceEdit
import org.eclipse.lsp4j.CodeAction

object LspCodeActionExecutor {

    fun execute(action: CodeAction, session: LspSession) {
        if (action.disabled != null) {
            thisLogger().error("Attempted to execute disabled code action: $action")
            return
        }

        action.edit?.also { edit ->
            applyWorkspaceEdit(session, edit, action.title)
        }

        action.command?.also { command ->
            LspCommandExecutionUtil.execute(command, session)
        }
    }

}
