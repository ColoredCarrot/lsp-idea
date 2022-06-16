package info.voidev.lspidea.plugins.bundled.rustanalyzer.config

import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.panel
import info.voidev.lspidea.config.Config
import javax.swing.JComponent

class RustAnalyzerConfigView : Config<RustAnalyzerConfigState> {

    private val mainComponent: DialogPanel

    lateinit var form: RustAnalyzerConfigForm
        private set

    init {
        @Suppress("UnstableApiUsage")
        mainComponent = panel {
            form = RustAnalyzerConfigForm(this)
//            UiFormBuilder(initOptions.cargo!!, initOptions.cargo!!.copy(), RustAnalyzerInitOptions.Cargo::class)
//                .apply { buildInto() }
        }
    }

    override fun getComponent(): DialogPanel {
        return mainComponent
    }

    override fun getPreferredFocusableComponent(): JComponent? {
        return null
    }

    override fun dispose() {
    }

    override fun apply(): RustAnalyzerConfigState {
        mainComponent.apply()
        return RustAnalyzerConfigState(
            initOptions = RustAnalyzerInitOptions(
                cargo = RustAnalyzerInitOptions.Cargo(
                    allFeatures = form.cargo.allFeatures,
                ),
                procMacro = RustAnalyzerInitOptions.ProcMacro(
                    enable = form.procMacro.enable,
                ),
                hover = RustAnalyzerInitOptions.Hover(
                    documentation = form.hover.documentation,
                    linksInHover = form.hover.linksInHover,
                ),
            )
        )
    }

    override fun reset(state: RustAnalyzerConfigState) {
        form.cargo.allFeatures = state.initOptions.cargo?.allFeatures
        form.procMacro.enable = state.initOptions.procMacro?.enable
        form.hover.documentation = state.initOptions.hover?.documentation
        form.hover.linksInHover = state.initOptions.hover?.linksInHover

        mainComponent.reset()
    }

    override fun createDefaults() = RustAnalyzerConfigState()
}
