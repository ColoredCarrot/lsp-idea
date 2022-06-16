package info.voidev.lspidea.config.serverhandler

import info.voidev.lspidea.config.Config
import info.voidev.lspidea.connect.LocalProcessLspRunnerProvider
import info.voidev.lspidea.def.LspServerSupport

class LocalProcessLspRunnerType : LspRunnerType<LocalProcessLspRunnerConfigState>(
    ID,
    "Local process",
    LocalProcessLspRunnerConfigState::class.java
) {

    override fun createConfig(origin: LspServerSupport<*>): Config<LocalProcessLspRunnerConfigState> {
        return LocalProcessLspRunnerConfig(origin)
    }

    override fun createRunnerProvider(state: LocalProcessLspRunnerConfigState) = LocalProcessLspRunnerProvider(state)

    companion object {
        const val ID = "builtin.localprocess"

        @JvmStatic
        val instance get() = get(ID)
    }
}
