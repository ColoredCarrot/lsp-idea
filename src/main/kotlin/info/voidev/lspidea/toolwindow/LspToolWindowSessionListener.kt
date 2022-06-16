package info.voidev.lspidea.toolwindow

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindowManager
import info.voidev.lspidea.connect.LspSession
import info.voidev.lspidea.event.LspSessionListener

/**
 * An [LspSessionListener] that creates tool windows for new sessions.
 */
class LspToolWindowSessionListener : LspSessionListener {

    override fun newSessionAtomic(session: LspSession) {
        ApplicationManager.getApplication().invokeLater({ run(session) }, ModalityState.any())
    }

    private fun run(session: LspSession) {
        val toolWindow = LspSessionToolWindow(session)

        val toolWindowContainer = ToolWindowManager.getInstance(session.project).getToolWindow("Language Server")!!

        val tab = toolWindowContainer.contentManager.factory.createContent(
            toolWindow.mainComponent,
            session.state.serverInfo.name,
            false
        )
        toolWindowContainer.contentManager.addContent(tab)

        // Whenever the tool window is disposed
        // (whether because the session finalizes or the tool window container is disposed),
        // also dispose the tool window
        Disposer.register(tab, toolWindow)

        // Whenever a session finalizes,
        // remove and dispose the tab
        Disposer.register(session) {
            if (!toolWindowContainer.contentManager.isDisposed) {
                toolWindowContainer.contentManager.removeContent(tab, true)
            }
        }
    }
}
