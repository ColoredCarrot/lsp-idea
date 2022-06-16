package info.voidev.lspidea.listeners

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor
import com.intellij.refactoring.suggested.range
import info.voidev.lspidea.LspIdea
import info.voidev.lspidea.LspSessionManager
import info.voidev.lspidea.connect.LspSession
import info.voidev.lspidea.dummy.LspDummyPsiElement
import info.voidev.lspidea.dummy.LspDummyPsiFile
import info.voidev.lspidea.editor.applyEdits
import info.voidev.lspidea.features.formatting.LspFormattingOptionsProvider
import info.voidev.lspidea.util.identifyForLsp
import info.voidev.lspidea.util.joinUnwrapExceptionsCancellable
import info.voidev.lspidea.util.offset2lspPosition
import info.voidev.lspidea.util.supportsRangeFormatting
import org.eclipse.lsp4j.DocumentFormattingParams
import org.eclipse.lsp4j.DocumentRangeFormattingParams
import org.eclipse.lsp4j.FormattingOptions
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.TextEdit
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException
import java.util.concurrent.CompletableFuture

// see https://intellij-support.jetbrains.com/hc/en-us/community/posts/206757245-Delegating-formatting-to-an-external-formatter
class LspPostFormatProcessor : PostFormatProcessor {

    override fun processElement(source: PsiElement, settings: CodeStyleSettings): PsiElement {
        if (source !is LspDummyPsiFile && source !is LspDummyPsiElement) {
            return source
        }

        processText(source.containingFile, source.textRange, settings)

        return source
    }

    override fun processText(source: PsiFile, rangeToReformat: TextRange, settings: CodeStyleSettings): TextRange {
        val file = source.virtualFile ?: return rangeToReformat
        val document = FileDocumentManager.getInstance().getDocument(file) ?: return rangeToReformat
        val session = LspSessionManager.getInstance(source.project).getForFile(file) ?: return rangeToReformat

        // TODO: We don't make use of settings here, is that an issue?
        val options = LspFormattingOptionsProvider.get(source)

        val entireFile = source.textRange == rangeToReformat

        if (!entireFile && !session.state.serverCapabilities.supportsRangeFormatting()) {
            logger.info("Language server does not support ranged formatting")
            return rangeToReformat
        }

        val rangeMarker =
            if (entireFile) null
            else document.createRangeMarker(rangeToReformat).apply { isGreedyToLeft = true; isGreedyToRight = true }

        doFormat(document, session, options, rangeToReformat.takeUnless { entireFile })

        val newRange =
            if (entireFile) TextRange(0, document.textLength)
            else rangeMarker?.range ?: TextRange.EMPTY_RANGE

        if (rangeMarker != null) {
            if (rangeMarker is Disposable) Disposer.dispose(rangeMarker)
            else rangeMarker.dispose()
        }

        return newRange
    }

    private fun doFormat(document: Document, session: LspSession, options: FormattingOptions, range: TextRange?) {
        val future =
            if (range == null) formatEntireFileRequest(document, session, options)
            else formatRangeRequest(document, session, range, options)

        val edits: List<TextEdit> = try {
            future.joinUnwrapExceptionsCancellable().orEmpty()
        } catch (ex: ResponseErrorException) {
            LspIdea.showResponseError("Failed to reformat file", ex.responseError, session.project)
            emptyList()
        }

        document.applyEdits(edits)
    }

    private fun formatEntireFileRequest(
        document: Document,
        session: LspSession,
        options: FormattingOptions,
    ): CompletableFuture<List<TextEdit>?> {
        return session.state.server.textDocumentService.formatting(
            DocumentFormattingParams(document.identifyForLsp(), options)
        )
    }

    private fun formatRangeRequest(
        document: Document,
        session: LspSession,
        range: TextRange,
        options: FormattingOptions,
    ): CompletableFuture<List<TextEdit>?> {
        return session.state.server.textDocumentService.rangeFormatting(
            DocumentRangeFormattingParams(
                document.identifyForLsp(),
                options,
                Range(
                    document.offset2lspPosition(range.startOffset),
                    document.offset2lspPosition(range.endOffset) // exclusive in IntelliJ and LSP
                )
            )
        )
    }

    companion object {
        private val logger = logger<LspPostFormatProcessor>()
    }
}
