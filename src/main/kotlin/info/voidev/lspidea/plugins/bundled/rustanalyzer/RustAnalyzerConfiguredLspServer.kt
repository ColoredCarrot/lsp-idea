package info.voidev.lspidea.plugins.bundled.rustanalyzer

import com.intellij.openapi.vfs.VirtualFile
import info.voidev.lspidea.def.AbstractConfiguredLspServer
import info.voidev.lspidea.def.LspServer
import info.voidev.lspidea.plugins.bundled.rustanalyzer.config.RustAnalyzerConfigState

class RustAnalyzerConfiguredLspServer(
    private val config: RustAnalyzerConfigState,
    server: LspServer,
) : AbstractConfiguredLspServer(server) {

    override val initOptions get() = config.initOptions

    override fun shouldActivateForFile(file: VirtualFile) =
        file.extension?.equals("rs", true) == true

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RustAnalyzerConfiguredLspServer) return false

        return config == other.config
    }

    override fun hashCode(): Int {
        return config.hashCode()
    }
}
