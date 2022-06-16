package info.voidev.lspidea.config

import info.voidev.lspidea.connect.LspRunnerProvider
import info.voidev.lspidea.def.ConfiguredLspServer

data class InstantiableLspServer(
    val definition: ConfiguredLspServer,
    val runnerProvider: LspRunnerProvider,
)
