package info.voidev.lspidea.command.clientside.std

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.ex.ActionUtil
import info.voidev.lspidea.command.clientside.LspCommandExecutorBase
import info.voidev.lspidea.connect.LspSession
import org.eclipse.lsp4j.Command

class LspStandardCommandExecutor : LspCommandExecutorBase("editor.action.triggerParameterHints") {

    override fun doExecute(command: Command, session: LspSession) {
        //FIXME
        // trigger IntelliJ's ShowParameterInfoAction
        //check com.intellij.openapi.actionSystem.ex.CheckboxAction.createCheckboxComponent
        val showParamsAction = ActionManager.getInstance().getAction("ParameterInfo")!!
        ActionUtil.invokeAction(showParamsAction, DataContext.EMPTY_CONTEXT, ActionPlaces.UNKNOWN, null, null)
    }

}
