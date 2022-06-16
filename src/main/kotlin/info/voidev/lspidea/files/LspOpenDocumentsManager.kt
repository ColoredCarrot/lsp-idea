package info.voidev.lspidea.files

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.refactoring.suggested.oldRange
import info.voidev.lspidea.connect.LspSession
import info.voidev.lspidea.util.identifyForLsp
import info.voidev.lspidea.util.range2lspRange
import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.DidCloseTextDocumentParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.TextDocumentContentChangeEvent
import org.eclipse.lsp4j.TextDocumentItem
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier

/**
 * Manages currently open text documents,
 * where open means that we sent an open notification to the server.
 */
//TODO support TextDocumentSyndKinds other than incremental
class LspOpenDocumentsManager(val session: LspSession) : Disposable {

    private val openDocuments = hashMapOf<String, LspOpenDocument>()

    init {
        Disposer.register(session, this)
    }

    fun getIfOpen(file: VirtualFile?): LspOpenDocument? {
        if (file == null) return null
        return openDocuments[file.identifyForLsp().uri]
    }

    @Synchronized
    fun notifyOpened(document: Document): LspOpenDocument {
        val uri = document.identifyForLsp().uri
        openDocuments[uri]?.also { return it }

        val file = FileDocumentManager.getInstance().getFile(document)
            ?: throw IllegalArgumentException("Cannot open purely in-memory document")

        val openDocument = LspOpenDocument(file)
        openDocuments[uri] = openDocument

        // Automatically dispose openDocument when the manager is disposed (along with the session)
        Disposer.register(this, openDocument)

        // Register our openDocument disposal handler
        Disposer.register(openDocument, MyDocumentDisposalHandler(openDocument))

        val documentDto = TextDocumentItem().also {
            it.uri = uri
            it.languageId = session.state.serverDef.language
            it.text = document.text
            it.version = openDocument.version
        }
        session.server.textDocumentService.didOpen(DidOpenTextDocumentParams(documentDto))

        // listener is automatically disposed with us
        document.addDocumentListener(MyDocumentListener(), openDocument)

        return openDocument
    }

    @Synchronized
    override fun dispose() {
        // We needn't do anything; all openDocuments are already registered as child disposables
        logger.assertTrue(openDocuments.isEmpty(), "openDocuments have not been disposed correctly")
    }

    private inner class MyDocumentDisposalHandler(private val openDocument: LspOpenDocument) : Disposable {
        override fun dispose() {
            synchronized(this@LspOpenDocumentsManager) {
                openDocuments.remove(openDocument.identifier.uri)
                if (session.isActive) {
                    session.server.textDocumentService.didClose(DidCloseTextDocumentParams(openDocument.identifier))
                }
            }
        }
    }

    private inner class MyDocumentListener : DocumentListener {
        private var imminentChange: DidChangeTextDocumentParams? = null

        override fun beforeDocumentChange(event: DocumentEvent) {
            // Construct DTO for change here because we won't be able to get accurate positions
            // after the change is complete
            val document = event.document
            val documentIdentifier = document.identifyForLsp()

            val openDocument = openDocuments[documentIdentifier.uri]
            if (openDocument == null) {
                logger.error("openDocument not found for non-disposed document listener. URI: ${documentIdentifier.uri}")
                return
            }

            val newVersion = openDocument.incrementVersion()

            val theChangeDto = TextDocumentContentChangeEvent().also {
                it.range = document.range2lspRange(event.oldRange)
                it.text = event.newFragment.toString()
                // Some language servers still rely on rangeLength
                it.rangeLength = event.oldLength
            }

            imminentChange = DidChangeTextDocumentParams().also {
                it.textDocument = VersionedTextDocumentIdentifier(documentIdentifier.uri, newVersion)
                it.contentChanges = listOf(theChangeDto)
            }
        }

        override fun documentChanged(event: DocumentEvent) {
            val dto = imminentChange
                ?: return logger.error("documentChanges without prepared change DTO")
            imminentChange = null

            session.server.textDocumentService.didChange(dto)
        }
    }

    companion object {
        private val logger = logger<LspOpenDocumentsManager>()
    }
}
