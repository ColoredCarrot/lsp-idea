package info.voidev.lspidea.config.servers

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.RoamingType
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.xmlb.annotations.Tag
import com.intellij.util.xmlb.annotations.XMap
import info.voidev.lspidea.LspIdea
import info.voidev.lspidea.LspSessionManager
import info.voidev.lspidea.config.InstantiableLspServer
import info.voidev.lspidea.config.runner.LspRunnerConfigStateInterface
import info.voidev.lspidea.config.runner.LspRunnerType
import info.voidev.lspidea.def.LspServerSupport
import java.util.concurrent.atomic.AtomicBoolean

@State(
    name = "lspidea.config.servers",
    storages = [
        Storage("lspidea.xml", roamingType = RoamingType.DISABLED),
    ],
)
@Service(Service.Level.APP)
internal class LspServersConfig : PersistentStateComponent<LspServersConfig.State> {

    private val updating = AtomicBoolean(false)

    data class State(
        @XMap
        var servers: MutableMap<String, Entry> = HashMap(),
    ) {
        data class Entry(
            @Tag
            var serverConfigProviderId: String = "",
            @Tag
            var state: LspServerConfigState = LspServerConfigState(),
        ) {
            fun createInstantiableServer(): InstantiableLspServer? {
                val configProvider = LspServerSupport.getById(serverConfigProviderId) as LspServerSupport<LspServerOptionsConfigStateInterface>?
                if (configProvider == null) {
                    LspIdea.showError(
                        "Missing Plugin",
                        "The language server with the ID \"${state.id}\" references missing support provider \"${serverConfigProviderId}\". Either the storage file was corrupted or a previously installed plugin has been uninstalled.",
                        null
                    )
                    return null
                }

                return InstantiableLspServer(
                    configProvider.createConfiguredServer(state.options.get()),
                    (state.runner.type!! as LspRunnerType<LspRunnerConfigStateInterface>).createRunnerProvider(state.runner.getConfig()!!)
                )
            }
        }
    }

    private var state = State()

    override fun getState() = state

    override fun loadState(state: State) {
        while (!updating.compareAndSet(false, true)) {
            // Basically a spin-lock.
            // A good fit because this scenario should never occur in practice.
        }

        try {
            this.state = state
            LspSessionManager.getAllInstances().forEach { it.destroyAll() }
        } finally {
            updating.set(false)
        }
    }

    fun isUpdating() = updating.get()

    companion object {
        @JvmStatic
        fun getInstance() = service<LspServersConfig>()

        @JvmStatic
        fun get() = getInstance().state

        @JvmStatic
        fun getInstantiableServers() = get().servers.mapNotNull { (_, entry) -> entry.createInstantiableServer() }
    }
}
