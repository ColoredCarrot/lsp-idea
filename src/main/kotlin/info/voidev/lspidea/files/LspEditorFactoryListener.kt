package info.voidev.lspidea.files

import com.intellij.openapi.editor.event.EditorFactoryListener

@Deprecated("replaced by LspFileDocumentManagerListener")
class LspEditorFactoryListener : EditorFactoryListener {

//    override fun editorCreated(event: EditorFactoryEvent) {
//        val project = event.editor.project ?: return
//        val file = FileDocumentManager.getInstance().getFile(event.editor.document) ?: return
//        val session = LspSessionManager.getInstance(project).getForFile(file) ?: return
//        session.openDocumentsManager.notifyOpened(event.editor.document)
//    }
//
//    override fun editorReleased(event: EditorFactoryEvent) {
//        val project = event.editor.project ?: return
//        val file = FileDocumentManager.getInstance().getFile(event.editor.document) ?: return
//        val session = LspSessionManager.getInstance(project).getForFile(file) ?: return
//        session.openDocumentsManager.notifyClosed(file)
//    }
}
