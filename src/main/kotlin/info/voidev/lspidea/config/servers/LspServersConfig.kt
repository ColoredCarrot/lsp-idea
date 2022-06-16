package info.voidev.lspidea.config.servers

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.RoamingType
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.xmlb.annotations.Tag
import com.intellij.util.xmlb.annotations.XMap
import info.voidev.lspidea.LspSessionManager
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
        )
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
    }
}
