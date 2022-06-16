package info.voidev.lspidea.toolwindow

import com.intellij.openapi.ui.ComponentContainer
import javax.swing.JComponent

class JrpcMessageDetailsView : ComponentContainer {

    lateinit var comp: JComponent

    override fun getComponent(): JComponent {
        return comp
    }

    override fun getPreferredFocusableComponent(): JComponent {
        return comp
    }

    override fun dispose() {
    }
}
