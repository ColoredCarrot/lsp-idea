package info.voidev.lspidea.plugins.bundled.generic

import info.voidev.lspidea.def.LspServerSupport

class GenericLspServerSupport : LspServerSupport<GenericLspServerConfigState> {

    override val id get() = ID

    override val server = GenericLspServer()

    override fun createConfig() = GenericLspServerConfig()

    override fun createConfigDefaults() = GenericLspServerConfigState()

    override fun createConfiguredServer(config: GenericLspServerConfigState) = GenericConfiguredLspServer(config, server)

    companion object {
        const val ID = "bundled.generic"
    }
}
