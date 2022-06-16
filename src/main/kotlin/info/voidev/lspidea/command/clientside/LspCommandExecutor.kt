package info.voidev.lspidea.command.clientside

import com.intellij.openapi.extensions.ExtensionPointName
import info.voidev.lspidea.connect.LspSession
import org.eclipse.lsp4j.Command

interface LspCommandExecutor {

    /**
     * If this command executor can execute [command],
     * executes it and returns `true`
     * (regardless of whether the execution itself was successful);
     * otherwise, returns `false`.
     */
    fun execute(command: Command, session: LspSession): Boolean

}

object LspCommandExecutorEP {

    @JvmStatic
    private val EP_NAME = ExtensionPointName.create<LspCommandExecutor>("info.voidev.lspidea.commandExecutor")

    fun execute(command: Command, session: LspSession): Boolean {
        return EP_NAME.extensionList.any { it.execute(command, session) }
    }

}
