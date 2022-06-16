package info.voidev.lspidea.command.clientside

import info.voidev.lspidea.connect.LspSession
import org.eclipse.lsp4j.Command

abstract class LspCommandExecutorBase(
    protected open vararg val supportedCommands: String,
) : LspCommandExecutor {

    protected abstract fun doExecute(command: Command, session: LspSession)

    override fun execute(command: Command, session: LspSession): Boolean {
        if (command.command !in supportedCommands) {
            return false
        }
        doExecute(command, session)
        return true
    }
}
