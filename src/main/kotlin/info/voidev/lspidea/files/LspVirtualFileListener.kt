package info.voidev.lspidea.files

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import info.voidev.lspidea.connect.LspSession
import info.voidev.lspidea.util.identifyForLsp
import org.eclipse.lsp4j.DidSaveTextDocumentParams

// automatically disposed with session, so session is always valid in this class
class LspVirtualFileListener(private val session: LspSession) : BulkFileListener {

    override fun after(events: MutableList<out VFileEvent>) {
        for (evt in events) {
            when (evt) {
                is VFileContentChangeEvent -> contentsChanged(evt)
            }
        }
    }

    private fun contentsChanged(evt: VFileContentChangeEvent) {
        if (!evt.isFromSave) {
            // Not interested in reload-from-disk events
            return
        }

        if (!ProjectFileIndex.getInstance(session.project).isInContent(evt.file)) {
            // Only watch files in the project
            return
        }

        // Send didSave if file is being tracked and server is interested

        val syncOptions = session.state.serverCapabilities.textDocumentSync?.right ?: return
        var shouldIncludeText = false
        if (syncOptions.save.isLeft) {
            if (!syncOptions.save.left) return
        } else {
            shouldIncludeText = syncOptions.save.right.includeText ?: false
        }

        // Include entire document contents if the server requests it
        var text: String? = null
        if (shouldIncludeText) {
            val document = FileDocumentManager.getInstance().getDocument(evt.file) ?: return
            text = document.text
        }

        session.server.textDocumentService.didSave(DidSaveTextDocumentParams(evt.file.identifyForLsp(), text))
    }
}
