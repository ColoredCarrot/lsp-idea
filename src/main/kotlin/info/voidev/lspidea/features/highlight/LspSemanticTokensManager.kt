package info.voidev.lspidea.features.highlight

import com.intellij.openapi.editor.Document
import info.voidev.lspidea.connect.LspSession
import info.voidev.lspidea.util.identifyForLsp
import info.voidev.lspidea.util.joinLsp
import info.voidev.lspidea.util.reverseConsecutiveSequences
import org.eclipse.lsp4j.SemanticTokens
import org.eclipse.lsp4j.SemanticTokensDelta
import org.eclipse.lsp4j.SemanticTokensDeltaParams
import org.eclipse.lsp4j.SemanticTokensEdit
import org.eclipse.lsp4j.SemanticTokensParams
import org.eclipse.lsp4j.jsonrpc.messages.Either
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

class LspSemanticTokensManager(private val session: LspSession) {

    private val semanticTokens = ConcurrentHashMap<String, SemanticTokensResult>()

    fun getSemanticTokens(document: Document) = semanticTokens[document.identifyForLsp().uri]?.tokens

    fun updateSemanticTokens(document: Document): List<LspToken>? {
        val documentId = document.identifyForLsp()

        var thereWereChanges = true

        val result = semanticTokens.compute(documentId.uri) { _, prevResult ->
            if (!mayRequest) return@compute null

            // Fetch either delta or full, depending on what we have and what the server supports
            val respFuture: CompletableFuture<Either<SemanticTokens, SemanticTokensDelta>> =
                if (prevResult?.resultId != null && mayRequestDelta) {
                    session.server.textDocumentService.semanticTokensFullDelta(
                        SemanticTokensDeltaParams(documentId, prevResult.resultId)
                    )
                } else {
                    session.server.textDocumentService.semanticTokensFull(SemanticTokensParams(documentId))
                        .thenApply { Either.forLeft(it) }
                }

            val resp = respFuture.joinLsp(session.project, "Could not fetch semantic tokens")
                ?: return@compute null

            val res = if (resp.isLeft) {
                makeResult(resp.left.data, resp.left.resultId, document)
            } else {
                val raw = applyUpdate(prevResult!!.raw, resp.right)
                makeResult(raw, resp.right.resultId, document)
            }

            thereWereChanges = res.raw != prevResult?.raw

            res
        }

        if (thereWereChanges) {
            session.project.messageBus.syncPublisher(LspSemanticTokensListener.TOPIC).didUpdate(document, result?.tokens)
        }

        return result?.tokens
    }

    private val mayRequest get() =
        session.state.serverCapabilities.semanticTokensProvider?.full?.let { it.left ?: it.isRight } == true

    private val mayRequestDelta get() =
        session.state.serverCapabilities.semanticTokensProvider?.full?.right?.delta == true

    /**
     * Computes the raw semantic tokens data resulting from
     * applying the given [delta] to [tokens].
     */
    private fun applyUpdate(tokens: List<Int>, delta: SemanticTokensDelta): List<Int> {
        val edits = delta.edits
        if (edits.isNullOrEmpty()) {
            return tokens
        }

        val sortedEdits: List<SemanticTokensEdit> =
            if (edits.size == 1) edits
            else ArrayList(edits).also { toSort ->
                toSort.sortByDescending { it.start }
                toSort.reverseConsecutiveSequences(Comparator.comparingInt { it.start })
            }

        val newTokens = ArrayList(tokens)
        for (edit in sortedEdits) {
            val range = newTokens.subList(edit.start, edit.start + edit.deleteCount)
            range.clear()
            range.addAll(edit.data)
        }

        return newTokens
    }

    private fun makeResult(raw: List<Int>, resultId: String?, document: Document) =
        SemanticTokensResult(
            raw,
            LspSemanticTokensParser.parse(raw, document, session),
            resultId
        )

    private data class SemanticTokensResult(
        val raw: List<Int>,
        val tokens: List<LspToken>,
        val resultId: String?,
    )
}
