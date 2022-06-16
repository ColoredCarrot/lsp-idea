package info.voidev.lspidea.features.highlight.ui

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBScrollPane

class LspSemanticTokensToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val view = LspSemanticTokensView(project)

        val content = toolWindow.contentManager.factory.createContent(
            JBScrollPane(view.component),
            null,
            false
        )

        Disposer.register(content, view)

        toolWindow.contentManager.addContent(content)
    }

    companion object {
        const val ID = "Semantic Tokens (LSP)"
    }
}
