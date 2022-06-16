package info.voidev.lspidea.toolwindow

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.ui.scale.JBUIScale
import com.intellij.util.ui.AbstractLayoutManager
import java.awt.Container
import java.awt.Dimension
import java.io.Writer
import javax.swing.JComponent
import javax.swing.JPanel

class TextOutputComponent(project: Project) : Disposable {

    val mainComponent: JComponent
    val output: Writer

    init {
        // see com.intellij.notification.EventLogConsole

        val editorComp = TextOutputEditorComponent(project)
        Disposer.register(this, editorComp)

        output = editorComp

        val editorPanel = JPanel(object : AbstractLayoutManager() {
            private val offset get() = JBUIScale.scale(4)

            override fun preferredLayoutSize(parent: Container): Dimension {
                val size = parent.getComponent(0).preferredSize
                return Dimension(size.width + offset, size.height)
            }

            override fun layoutContainer(parent: Container) {
                val offset = offset
                parent.getComponent(0).setBounds(offset, 0, parent.width - offset, parent.height)
            }
        })
        editorPanel.background = editorComp.editor.backgroundColor
        editorPanel.add(editorComp.editor.component)

        mainComponent = editorPanel
    }

    override fun dispose() {
    }
}
