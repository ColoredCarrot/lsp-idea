package info.voidev.lspidea.def

import com.intellij.openapi.vfs.VirtualFile
import info.voidev.lspidea.LspIdea
import info.voidev.lspidea.config.InstantiableLspServer
import info.voidev.lspidea.config.serverhandler.LspRunnerConfigStateInterface
import info.voidev.lspidea.config.serverhandler.LspRunnerType
import info.voidev.lspidea.config.servers.LspServerOptionsConfigStateInterface
import info.voidev.lspidea.config.servers.LspServersConfig
import java.util.UUID
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
            servers = computeServers()
            cachedServers.set(servers)
        }

        return servers
    }

    private fun computeServers(): List<InstantiableLspServer> {
        val instances = ArrayList<InstantiableLspServer>()

        LspServersConfig.get().servers.forEach { (idRaw, entry) ->
            val id = UUID.fromString(idRaw)

            val configProvider = LspServerSupport.getById(entry.serverConfigProviderId) as LspServerSupport<LspServerOptionsConfigStateInterface>?
            if (configProvider == null) {
                LspIdea.showError(
                    "Missing Plugin",
                    "The language server with the ID \"$id\" references missing support provider \"${entry.serverConfigProviderId}\". Either the storage file was corrupted or a previously installed plugin has been uninstalled.",
                    null
                )
                return@forEach
            }

            instances += InstantiableLspServer(
                configProvider.createConfiguredServer(entry.state.options.get()),
                (entry.state.runner.type!! as LspRunnerType<LspRunnerConfigStateInterface>).createRunnerProvider(entry.state.runner.getConfig()!!)
            )
        }

        return instances
    }

    override fun getForFile(file: VirtualFile): InstantiableLspServer? {
        return getServers().firstOrNull { it.definition.shouldActivateForFile(file) }
    }
}
