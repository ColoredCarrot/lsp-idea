package info.voidev.lspidea.plugins.bundled.rustanalyzer.config

import com.intellij.ui.dsl.builder.Panel
import info.voidev.lspidea.util.ui.Form

@Suppress("UnstableApiUsage")
class RustAnalyzerConfigForm(panel: Panel) : Form(panel) {

    val cargo = nested("Cargo", RustAnalyzerConfigForm::Cargo)
    val procMacro = nested("Proc macro", RustAnalyzerConfigForm::ProcMacro)
    val hover = nested("Hover", RustAnalyzerConfigForm::Hover)

    class Cargo(panel: Panel) : Form(panel) {
        var allFeatures by checkBoxOrDefault("All features")
    }

    class ProcMacro(panel: Panel) : Form(panel) {
        var enable by checkBoxOrDefault("Enable")
    }

    class Hover(panel: Panel) : Form(panel) {
        var documentation by checkBoxOrDefault("Documentation")
        var linksInHover by checkBoxOrDefault("Links in hover")
    }

}
