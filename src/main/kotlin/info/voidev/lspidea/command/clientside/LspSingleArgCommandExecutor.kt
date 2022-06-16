package info.voidev.lspidea.command.clientside

import info.voidev.lspidea.connect.LspSession
import org.eclipse.lsp4j.Command
import java.lang.reflect.Type

abstract class LspSingleArgCommandExecutor<T>(vararg supportedCommands: String, private val argType: Type) :
    LspCommandExecutorBase(*supportedCommands) {

    protected abstract fun execute(arg: T, command: Command, session: LspSession)

    override fun doExecute(command: Command, session: LspSession) {
        val rawArg = command.arguments.singleOrNull()
            ?: throw LspCommandExecutionException("Single-parameter command '${command.command}' submitted with ${command.arguments.size} arguments")

        val gson = session.state.debugger.gson
        val arg = gson.fromJson<T>(gson.toJsonTree(rawArg), argType)

        execute(arg, command, session)
    }
}
