package info.voidev.lspidea.plugins.bundled.generic

import com.intellij.openapi.vfs.VirtualFile
import info.voidev.lspidea.def.AbstractConfiguredLspServer
import info.voidev.lspidea.def.LspServer

class GenericConfiguredLspServer(
    private val config: GenericLspServerConfigState,
    server: LspServer
) : AbstractConfiguredLspServer(server) {

    override val initOptions: Nothing? get() = null

    override fun shouldActivateForFile(file: VirtualFile): Boolean {
        val pattern = config.filenamePattern ?: return false
        val path = file.fileSystem.getNioPath(file) ?: return false

        return pattern matches path
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GenericConfiguredLspServer) return false

        return config == other.config
    }

    override fun hashCode(): Int {
        return config.hashCode()
    }
}
