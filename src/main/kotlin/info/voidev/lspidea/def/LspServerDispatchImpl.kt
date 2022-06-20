package info.voidev.lspidea.def

import com.intellij.openapi.vfs.VirtualFile
import info.voidev.lspidea.config.InstantiableLspServer
import info.voidev.lspidea.config.servers.LspServersConfig
import java.util.concurrent.atomic.AtomicReference

class LspServerDispatchImpl : LspServerDispatch {

    private var cachedServers = AtomicReference<List<InstantiableLspServer>?>(null)

    private fun getServers(): List<InstantiableLspServer> {
        val serversConfig = LspServersConfig.getInstance()

        // Crudely suspend active features while the server list is changing
        if (serversConfig.isUpdating()) {
            cachedServers.set(null)
            return emptyList()
        }

        var servers = cachedServers.get()
        if (servers == null) {
            servers = LspServersConfig.getInstantiableServers()
            cachedServers.set(servers)
        }

        return servers
    }

    override fun getForFile(file: VirtualFile): InstantiableLspServer? {
        return getServers().firstOrNull { it.definition.shouldActivateForFile(file) }
    }
}
