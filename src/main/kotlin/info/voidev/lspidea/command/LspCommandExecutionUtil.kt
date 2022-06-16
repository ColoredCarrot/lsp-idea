package info.voidev.lspidea.command

import info.voidev.lspidea.LspIdea
import info.voidev.lspidea.command.clientside.LspCommandExecutorEP
import info.voidev.lspidea.connect.LspSession
import org.eclipse.lsp4j.Command
import org.eclipse.lsp4j.ExecuteCommandParams
import java.util.UUID

object LspCommandExecutionUtil {

    private fun isSupportedByServer(command: String, session: LspSession) =
        session.state.serverCapabilities.executeCommandProvider?.commands?.contains(command) == true

    fun execute(command: Command, session: LspSession) {
        if (isSupportedByServer(command.command, session)) {
            session.server.workspaceService.executeCommand(ExecuteCommandParams().apply {
                // We have the luxury of just generating a random work done token,
                // since we only create work done state on our side
                // when we get a "begin work" notification
                setWorkDoneToken(UUID.randomUUID().toString())
                this.command = command.command
                this.arguments = command.arguments
            })
            return
        }

        val wasExecuted = LspCommandExecutorEP.execute(command, session)
        if (wasExecuted) {
            return
        }

        // No way to execute the command!
        LspIdea.showError(
            "No way to execute \"${command.title}\"",
            "Neither ${session.state.serverInfo.name} nor the client supports executing \"${command.command}\"",
            session.project
        )
    }

}
