package info.voidev.lspidea.plugins.bundled.rustanalyzer

import info.voidev.lspidea.def.LspServerSupport
import info.voidev.lspidea.plugins.bundled.rustanalyzer.config.RustAnalyzerConfigState
import info.voidev.lspidea.plugins.bundled.rustanalyzer.config.RustAnalyzerConfigView
import info.voidev.lspidea.plugins.bundled.rustanalyzer.download.RustAnalyzerInstaller

class RustAnalyzerSupport : LspServerSupport<RustAnalyzerConfigState> {

    override val id get() = ID

    override val server = RustAnalyzerLspServer()

    override fun createConfig() = RustAnalyzerConfigView()

    override fun createConfigDefaults() = RustAnalyzerConfigState()

    override fun createConfiguredServer(config: RustAnalyzerConfigState) = RustAnalyzerConfiguredLspServer(config, server)

    override val installer get() = RustAnalyzerInstaller()

    companion object {
        const val ID = "bundled.rust-analyzer"
    }
}
