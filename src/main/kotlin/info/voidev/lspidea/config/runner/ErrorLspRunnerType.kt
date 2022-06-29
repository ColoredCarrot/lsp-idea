package info.voidev.lspidea.config.runner

import com.intellij.ui.ColoredText
import com.intellij.ui.SimpleColoredComponent
import com.intellij.ui.SimpleTextAttributes
import info.voidev.lspidea.config.Config
import info.voidev.lspidea.connect.LspRunnerProvider
import info.voidev.lspidea.def.LspServerSupport
import javax.swing.JComponent

/**
 * Used if a server handler type was requested for an unknown ID.
 *
 * This usually happens if the user has uninstalled a support plugin
 * that used to provide the now-missing server handler type.
 *
 * Transparently uses the missing ID so as not to delete any configuration
 * in case the support plugin is reinstalled.
 */
// TODO: We might in the future cache the providing plugin together with the server handler type ID
//  to be able to show a message like "reinstall plugin X";
//  see PluginManager.getPluginByClassName()
class ErrorLspRunnerType(id: String) : LspRunnerType<LspRunnerConfigStateInterface>(
    id,
    ColoredText.singleFragment("Error", SimpleTextAttributes.ERROR_ATTRIBUTES),
    LspRunnerConfigStateInterface::class.java // TODO
) {

    override fun createConfig(origin: LspServerSupport<*>): Config<LspRunnerConfigStateInterface> {
        return object : Config<LspRunnerConfigStateInterface> {
            private val comp = SimpleColoredComponent()
            private var errorState: LspRunnerConfigStateInterface? = null

            init {
                val errorStyle = SimpleTextAttributes.ERROR_ATTRIBUTES
                val boldErrorStyle = errorStyle.derive(SimpleTextAttributes.STYLE_BOLD, null, null, null)
                comp.append("Error: ", boldErrorStyle)
                comp.append(
                    """Missing language server process handler type $id. 
                    |This is usually caused by a missing support plugin. 
                    |Reinstalling the plugin will recover the configuration.""".trimMargin(),
                    errorStyle
                )
            }

            override fun apply() = errorState ?: object : LspRunnerConfigStateInterface {}

            override fun reset(state: LspRunnerConfigStateInterface) {
                errorState = state
            }

            override fun getComponent() = comp

            override fun getPreferredFocusableComponent(): JComponent? = null

            override fun createDefaults() = apply()

            override fun dispose() {
            }
        }
    }

    override fun createRunnerProvider(state: LspRunnerConfigStateInterface): LspRunnerProvider {
        error("Missing server handler type")
    }
}
