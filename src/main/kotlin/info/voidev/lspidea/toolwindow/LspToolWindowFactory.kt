package info.voidev.lspidea.toolwindow

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBPanelWithEmptyText
import javax.swing.JComponent

class LspToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun isApplicable(project: Project) = true

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        toolWindow.contentManager.addContent(
            toolWindow.contentManager.factory.createContent(
                createOverview(),
                "Overview",
                false
            )
        )

        // More contents will be added dynamically (whenever a session is created)
    }

    private fun createOverview(): JComponent {
        val panel = JBPanelWithEmptyText()

        return panel
    }
}
