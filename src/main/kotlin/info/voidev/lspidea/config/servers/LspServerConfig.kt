package info.voidev.lspidea.config.servers

import com.intellij.openapi.util.Disposer
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.panels.VerticalLayout
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.util.ui.UIUtil
import info.voidev.lspidea.config.Config
import info.voidev.lspidea.config.serverhandler.LspRunnerConfig
import info.voidev.lspidea.def.LspServerSupport
import java.util.UUID
import javax.swing.JComponent
import javax.swing.JPanel

class LspServerConfig(val origin: LspServerSupport<*>) : Config<LspServerConfigState> {

    lateinit var id: UUID
        private set

    private val mainComponent = JPanel(VerticalLayout(UIUtil.DEFAULT_VGAP))

    private val nameField = JBTextField()

    private val runnerConfig = LspRunnerConfig(origin)

    private val optionsConfig = origin.createConfig()

    val liveGivenName get() = nameField.text.trim()

    init {
        Disposer.register(this, runnerConfig)

        nameField.setTextToTriggerEmptyTextStatus(origin.server.displayName)
        nameField.emptyText.text = origin.server.displayName

        @Suppress("UnstableApiUsage")
        mainComponent.add(
            panel {
                row {
                    label("Name:")
                    cell(nameField).horizontalAlign(HorizontalAlign.FILL)
                }
            }
        )
        mainComponent.add(runnerConfig.component)
        mainComponent.add(optionsConfig.component)
    }

    override fun getComponent(): JComponent {
        return mainComponent
        // TODO support plugin-specific settings here, below the runner settings
    }

    override fun getPreferredFocusableComponent(): JComponent? {
        return runnerConfig.preferredFocusableComponent ?: nameField
    }

    override fun apply(): LspServerConfigState {
        return LspServerConfigState(
            id = id,
            name = nameField.text.trim(),
            runner = runnerConfig.apply(),
            options = LspServerOptionsConfigState(optionsConfig.apply())
        )
    }

    override fun reset(state: LspServerConfigState) {
        id = state.id
        nameField.text = state.name
        runnerConfig.reset(state.runner)
        (optionsConfig as Config<LspServerOptionsConfigStateInterface>).reset(state.options.get())
    }

    override fun createDefaults() = LspServerConfigState(
        name = origin.server.displayName,
        runner = runnerConfig.createDefaults(),
        options = LspServerOptionsConfigState(optionsConfig.createDefaults())
    )

    override fun dispose() {
    }
}
