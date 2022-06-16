package info.voidev.lspidea.config

import com.intellij.openapi.options.ConfigurationException
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import info.voidev.lspidea.util.getValue
import info.voidev.lspidea.util.setValue
import javax.swing.JPanel

class LspIdeaConfigComponent {

    val panel: JPanel
    private val fooTextBox = JBTextField()
    private val maxSelectionRangesBox = JBTextField()

    init {
        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent("Foo value: ", fooTextBox, 1, false)
            .addLabeledComponent("Max. selection ranges", maxSelectionRangesBox)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    var foo by fooTextBox

    var maxSelectionRanges: Int
        get() {
            return maxSelectionRangesBox.text.toIntOrNull() ?: throw ConfigurationException("Not a number")
        }
        set(value) {
            maxSelectionRangesBox.text = value.toString()
        }

}
