package info.voidev.lspidea.features.codeaction

import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.TextRange
import info.voidev.lspidea.connect.LspSession
import info.voidev.lspidea.util.identifyForLsp
import org.eclipse.lsp4j.CodeAction
import java.util.concurrent.ConcurrentHashMap

class LspCodeActionManager(session: LspSession) : Disposable {

    // TODO: This might be a possible memory leak
    //  if the user just opens more and more documents
    //  and getAvailableActions is never queried for them
    private val availableActions =
        ConcurrentHashMap<String, Pair<GenerationalDocumentRangeReference, Collection<CodeAction>>>()

    init {
        Disposer.register(session, this)
    }

    fun getAvailableActions(document: Document, range: TextRange): Collection<CodeAction> {
        val canonicalUrl = document.identifyForLsp().uri

        val (generationalRef, actions) = availableActions.computeIfPresent(canonicalUrl) { _, (generationalRef, actions) ->
            if (generationalRef.documentModificationStamp != document.modificationStamp) {
                // Outdated data
                null
            } else {
                generationalRef to actions
            }
        } ?: return emptyList()

        // Only return the actions if they are valid for the given range
        if (range !in generationalRef.range) {
            return emptyList()
        }

        return actions
    }

    fun setAvailableActions(document: Document, range: TextRange, actions: Collection<CodeAction>) {
        val canonicalUrl = document.identifyForLsp().uri
        availableActions[canonicalUrl] =
            GenerationalDocumentRangeReference(canonicalUrl, document.modificationStamp, range) to actions
    }

    override fun dispose() {
        availableActions.clear() // Why not. Though we should hopefully be deallocated immediately anyway
    }
}
