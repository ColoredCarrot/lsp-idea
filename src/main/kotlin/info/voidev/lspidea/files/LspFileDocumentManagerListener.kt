package info.voidev.lspidea.files

import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManagerListener
import com.intellij.openapi.project.ProjectLocator
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import info.voidev.lspidea.LspSessionManager

class LspFileDocumentManagerListener : FileDocumentManagerListener {

    override fun fileContentLoaded(file: VirtualFile, document: Document) {
        // A document has been created from a file
        for (project in ProjectLocator.getInstance().getProjectsForFile(file)) {
            // Get the session for the file only if it exists already
            val session = LspSessionManager.getInstanceIfCreated(project)?.getForIfActive(file) ?: continue
            // Notify the session's open documents manager that a document has been created
            session.openDocumentsManager.notifyOpened(document)
        }
    }

    override fun afterDocumentUnbound(file: VirtualFile, document: Document) {
        // A document associated with a file has been destroyed
        for (project in ProjectLocator.getInstance().getProjectsForFile(file)) {
            val session = LspSessionManager.getInstanceIfCreated(project)?.getForIfActive(file) ?: continue
            val openDocument = session.openDocumentsManager.getIfOpen(file) ?: continue
            Disposer.dispose(openDocument)
        }
    }

    override fun fileContentReloaded(file: VirtualFile, document: Document) {
        // Not needed: DocumentListener is invoked
    }

    override fun beforeAllDocumentsSaving() {
        // Not needed: this.beforeDocumentSaving is called for each document anyway
    }

    override fun beforeDocumentSaving(document: Document) {
        // TODO: Support this part of the LSP
    }

    override fun fileWithNoDocumentChanged(file: VirtualFile) {
        // TODO Check if this is really not needed
    }

    override fun unsavedDocumentDropped(document: Document) {
        // TODO Check if this is really not needed
    }

}
