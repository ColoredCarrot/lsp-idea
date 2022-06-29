package info.voidev.lspidea.config.runner

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.roots.ui.whenItemSelected
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.util.Disposer
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.components.JBPanelWithEmptyText
import com.intellij.ui.components.panels.VerticalLayout
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.UIUtil
import info.voidev.lspidea.config.Config
import info.voidev.lspidea.def.LspServerSupport
import info.voidev.lspidea.ui.OverlayLayout
import java.util.Vector
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.JPanel

class LspRunnerConfig(val origin: LspServerSupport<*>) : Config<LspRunnerConfigState> {

    private val typeComboBoxModel = DefaultComboBoxModel(Vector(LspRunnerType.getAllAvailable()))
    private val typeComboBox = ComboBox(typeComboBoxModel)

    private val container = JBPanelWithEmptyText(OverlayLayout()) // just any layout to span the entire space

    private val mainComponent: JComponent

    private var activeType: LspRunnerType<*>? = null
    private var activeConfig: Config<out LspRunnerConfigStateInterface>? = null

    init {
        typeComboBox.renderer = RunnerTypeRenderer()
        typeComboBox.whenItemSelected(::switchHandlerType)

        mainComponent = JPanel(VerticalLayout(UIUtil.DEFAULT_VGAP))

        @Suppress("UnstableApiUsage")
        mainComponent.add(
            panel {
                row {
                    label("Server origin:")
                    cell(typeComboBox)
                }
            }
        )

        mainComponent.add(container)

        // Make sure the first option is selected by default
        typeComboBox.selectedIndex = 0
    }

    private fun switchHandlerType(newHandlerType: LspRunnerType<*>?) {
        ApplicationManager.getApplication().assertIsDispatchThread()

        if (activeType == newHandlerType) {
            return
        }

        if (activeType != null) {
            container.removeAll()
            Disposer.dispose(activeConfig!!)
        }

        if (newHandlerType == null) {
            activeType = null
            activeConfig = null
            return
        }

        val newConfig = newHandlerType.createConfig(origin)
        container.add(newConfig.component)
        newConfig.preferredFocusableComponent?.requestFocusInWindow()

        activeConfig = newConfig
        activeType = newHandlerType
    }

    override fun getComponent(): JComponent = mainComponent

    override fun getPreferredFocusableComponent() = activeConfig?.preferredFocusableComponent

    override fun dispose() {
        switchHandlerType(null)
    }

    override fun apply(): LspRunnerConfigState {
        return LspRunnerConfigState(
            activeType,
            activeConfig?.apply()
        )
    }

    override fun reset(state: LspRunnerConfigState) {
        switchHandlerType(state.type)
        if (activeConfig != null) {
            (activeConfig as Config<LspRunnerConfigStateInterface>).reset(state.getConfig()!!)
        }
    }

    override fun createDefaults(): LspRunnerConfigState {
        return LspRunnerConfigState(
            LocalProcessLspRunnerType.instance,
            LocalProcessLspRunnerConfigState()
        )
    }

    private class RunnerTypeRenderer : ColoredListCellRenderer<LspRunnerType<*>>() {
        override fun customizeCellRenderer(
            list: JList<out LspRunnerType<*>>,
            value: LspRunnerType<*>,
            index: Int,
            selected: Boolean,
            hasFocus: Boolean,
        ) {
            append(value.displayName)
        }
    }
}
