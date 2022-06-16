package info.voidev.lspidea.config.serverhandler

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.ui.ColoredText
import info.voidev.lspidea.LspIdea
import info.voidev.lspidea.config.Config
import info.voidev.lspidea.connect.LspRunnerProvider
import info.voidev.lspidea.def.LspServerSupport
import org.jetbrains.annotations.Nls

abstract class LspRunnerType<S : LspRunnerConfigStateInterface>(
    val id: String,
    val displayName: @Nls(capitalization = Nls.Capitalization.Sentence) ColoredText,
    val stateClass: Class<S>,
) {

    constructor(
        id: String,
        displayName: @Nls(capitalization = Nls.Capitalization.Sentence) String,
        stateClass: Class<S>,
    ) : this(id, ColoredText.singleFragment(displayName), stateClass)

    abstract fun createConfig(origin: LspServerSupport<*>): Config<S>

    abstract fun createRunnerProvider(state: S): LspRunnerProvider

    companion object {
        @JvmStatic
        val EP_NAME = ExtensionPointName<LspRunnerType<*>>(LspIdea.EP_PREFIX + "serverHandlerType")

        @JvmStatic
        fun getAllAvailable(): List<LspRunnerType<*>> = EP_NAME.extensionList

        @JvmStatic
        fun get(id: String): LspRunnerType<*> {
            return getOrNull(id) ?: ErrorLspRunnerType(id)
        }

        @JvmStatic
        fun getOrNull(id: String): LspRunnerType<*>? {
            return EP_NAME.findFirstSafe { it.id == id }
        }
    }
}
