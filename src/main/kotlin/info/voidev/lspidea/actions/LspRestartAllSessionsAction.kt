package info.voidev.lspidea.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import info.voidev.lspidea.LspSessionManager

class LspRestartAllSessionsAction : AnAction() {

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project!!
        LspSessionManager.getInstance(project).destroyAll()
    }

}
