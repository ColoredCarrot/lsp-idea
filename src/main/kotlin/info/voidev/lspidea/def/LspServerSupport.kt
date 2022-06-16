package info.voidev.lspidea.def

import com.intellij.openapi.extensions.ExtensionPointName
import info.voidev.lspidea.LspIdea
import info.voidev.lspidea.config.Config
import info.voidev.lspidea.config.servers.LspServerOptionsConfigStateInterface
import info.voidev.lspidea.download.LspServerExecutableInstaller

interface LspServerSupport<C : LspServerOptionsConfigStateInterface> {

    /**
     * An identifier that must be unique across all language servers
     * and different plugins supporting the same language server.
     *
     * Usually, your plugin's base package name is a good choice.
     */
    val id: String

    val server: LspServer

    fun createConfig(): Config<C>

    fun createConfigDefaults(): C

    fun createConfiguredServer(config: C): ConfiguredLspServer

    val installer: LspServerExecutableInstaller? get() = null

    companion object {
        @JvmStatic
        val EP_NAME = ExtensionPointName<LspServerSupport<*>>(LspIdea.EP_PREFIX + "serverSupport")

        @JvmStatic
        fun getById(id: String): LspServerSupport<*>? {
            return EP_NAME.extensionList.firstOrNull { it.id == id }
        }
    }
}
