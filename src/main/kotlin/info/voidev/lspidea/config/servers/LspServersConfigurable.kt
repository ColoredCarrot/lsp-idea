package info.voidev.lspidea.config.servers

import com.intellij.openapi.extensions.BaseExtensionPointName
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.util.Disposer
import info.voidev.lspidea.LspIdea
import info.voidev.lspidea.def.LspServerSupport
import java.util.UUID
import javax.swing.JComponent

class LspServersConfigurable : Configurable, Configurable.WithEpDependencies {

    private var view: LspServersConfigView? = null

    override fun getDependencies(): Collection<BaseExtensionPointName<*>> =
        listOf(LspServerSupport.EP_NAME)

    override fun createComponent(): JComponent {
        view = LspServersConfigView()
        return view!!.component
    }

    override fun getPreferredFocusedComponent() = view?.preferredFocusableComponent

    override fun isModified(): Boolean {
        // TODO: Improve performance by adding dirty flag
        //  that is set to true on any simplistic change
        val storedState = LspServersConfig.get()

        val configs = view!!.serverConfigs
        if (configs.size != storedState.servers.size) {
            return true
        }

        for (config in configs) {
            val storedConfig = storedState.servers[config.id.toString()] ?: return true
            val storedConfigState = storedConfig.state

            val uiState = config.apply()
            if (uiState != storedConfigState) {
                return true
            }
        }

        return false
    }

    override fun apply() {
        val uiState = getApply()
        LspServersConfig.getInstance().loadState(uiState)
    }

    private fun getApply(): LspServersConfig.State {
        return LspServersConfig.State(
            view!!.serverConfigs.associateTo(HashMap()) { serverConfig ->
                val newState = serverConfig.apply()

                serverConfig.id.toString() to LspServersConfig.State.Entry(
                    serverConfig.origin.id,
                    newState
                )
            }
        )
    }

    override fun reset() {
        // Replace the current server list with what we have stored

        val serverConfigs = ArrayList<LspServerConfig>()

        LspServersConfig.get().servers.forEach { (idRaw, entry) ->
            val id = UUID.fromString(idRaw)

            val configProvider = LspServerSupport.getById(entry.serverConfigProviderId)
            if (configProvider == null) {
                LspIdea.showError(
                    "Missing Plugin",
                    "The language server with the ID \"$id\" references missing support provider \"${entry.serverConfigProviderId}\". Either the storage file was corrupted or a previously installed plugin was uninstalled.",
                    null
                )
                return@forEach
            }

            val serverConfig = LspServerConfig(configProvider)
            serverConfig.reset(entry.state)

            serverConfigs += serverConfig
        }

        view!!.serverConfigs = serverConfigs
    }

    override fun getDisplayName() = "Language Servers"

    override fun disposeUIResources() {
        view?.also {
            Disposer.dispose(it)
            view = null
        }
    }
}
