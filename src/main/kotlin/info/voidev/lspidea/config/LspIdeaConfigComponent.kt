package info.voidev.lspidea.config

import com.intellij.openapi.options.ConfigurationException
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JPanel

class LspIdeaConfigComponent {

    val panel: JPanel
    private val maxSelectionRangesBox = JBTextField()

    init {
        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent("Max. selection ranges", maxSelectionRangesBox)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    var maxSelectionRanges: Int
        get() {
            return maxSelectionRangesBox.text.toIntOrNull() ?: throw ConfigurationException("Not a number")
        }
        set(value) {
            maxSelectionRangesBox.text = value.toString()
        }
}
