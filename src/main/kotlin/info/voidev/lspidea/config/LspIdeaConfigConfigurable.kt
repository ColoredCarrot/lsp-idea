package info.voidev.lspidea.config

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import javax.swing.JComponent

class LspIdeaConfigConfigurable(private val project: Project) : Configurable {

    private var component: LspIdeaConfigComponent? = null

    override fun createComponent(): JComponent {
        component = LspIdeaConfigComponent()
        return component!!.panel
    }

    override fun isModified(): Boolean {
        val comp = component!!
        val config = LspIdeaConfig.getInstance(project).state
        return try {
            comp.foo != config.fooValue ||
                    comp.maxSelectionRanges != config.maxSelectionRanges
        } catch (_: ConfigurationException) {
            true
        }
    }

    override fun apply() {
        val comp = component!!
        val config = LspIdeaConfig.getInstance(project).state
        config.fooValue = comp.foo
        config.maxSelectionRanges = comp.maxSelectionRanges
    }

    override fun reset() {
        val comp = component!!
        val config = LspIdeaConfig.getInstance(project).state
        comp.foo = config.fooValue
        comp.maxSelectionRanges = config.maxSelectionRanges
    }

    override fun disposeUIResources() {
        component = null
    }

    override fun getDisplayName() = "LSP-IDEA"

}
