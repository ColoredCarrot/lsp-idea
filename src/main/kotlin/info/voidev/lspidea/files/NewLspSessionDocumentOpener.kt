package info.voidev.lspidea.files

import com.intellij.openapi.editor.EditorFactory
import info.voidev.lspidea.LspSessionManager
import info.voidev.lspidea.connect.LspSession
import info.voidev.lspidea.event.LspSessionListener

class NewLspSessionDocumentOpener : LspSessionListener {
    override fun newSession(session: LspSession) {

        val sessionMan = LspSessionManager.getInstance(session.project)

        // Open all documents open in IntelliJ (this is really hard actually)
        EditorFactory.getInstance().allEditors.forEach { editor ->
            if (editor.project == session.project) {
                val thatSession = sessionMan.getFor(editor.document)
                if (thatSession === session) {
                    session.openDocumentsManager.notifyOpened(editor.document)
                }
            }
        }
    }
}
