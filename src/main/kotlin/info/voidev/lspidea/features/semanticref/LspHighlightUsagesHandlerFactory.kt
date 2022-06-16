package info.voidev.lspidea.features.semanticref

import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerBase
import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerFactory
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.Consumer
import info.voidev.lspidea.LspSessionManager
import info.voidev.lspidea.dummy.LspDummyPsiFile
import info.voidev.lspidea.util.caretLspPosition
import info.voidev.lspidea.util.identifyForLsp
import info.voidev.lspidea.util.joinLsp
import info.voidev.lspidea.util.lspRange2range
import org.eclipse.lsp4j.DocumentHighlight
import org.eclipse.lsp4j.DocumentHighlightKind
import org.eclipse.lsp4j.DocumentHighlightParams
import org.eclipse.lsp4j.ServerCapabilities

/**
 * A document highlighter based on the LSP `textDocument/documentHighlight` request.
 * Highlights related text ranges in the same file,
 * e.g. occurrences of the variable under the caret.
 */
class LspHighlightUsagesHandlerFactory : HighlightUsagesHandlerFactory {

    override fun createHighlightUsagesHandler(editor: Editor, file: PsiFile): HighlightUsagesHandlerBase<*>? {
        if (file !is LspDummyPsiFile) return null
        val vfile = file.virtualFile ?: return null

        return object : HighlightUsagesHandlerBase<PsiElement>(editor, file) {
            override fun getTargets() = listOf<PsiElement>(file)

            override fun computeUsages(targets: MutableList<out PsiElement>) {
                if (!thisLogger().assertTrue(targets.singleOrNull() === file)) {
                    return
                }

                myReadUsages.clear()
                myWriteUsages.clear()

                val session = LspSessionManager.getInstance(file.project).getForFile(vfile)
                    ?: return

                if (!supportsDocumentHighlighting(session.state.serverCapabilities)) {
                    return
                }

                val highlights: List<DocumentHighlight> =
                    session.server.textDocumentService.documentHighlight(DocumentHighlightParams(
                        vfile.identifyForLsp(),
                        editor.caretLspPosition
                    )).joinLsp(session.project, "Could not fetch document highlights")
                        ?: return

                for (highlight in highlights) {
                    (if (highlight.kind == DocumentHighlightKind.Write) myWriteUsages else myReadUsages)
                        .add(editor.document.lspRange2range(highlight.range))
                }
            }

            private fun supportsDocumentHighlighting(caps: ServerCapabilities): Boolean {
                val provider = caps.documentHighlightProvider ?: return false
                return provider.left ?: provider.isRight
            }

            override fun selectTargets(
                targets: MutableList<out PsiElement>,
                selectionConsumer: Consumer<in MutableList<out PsiElement>>,
            ) {
                selectionConsumer.consume(
                    targets.filterIsInstanceTo<LspDummyPsiFile, MutableList<PsiElement>>(ArrayList())
                )
            }
        }
    }

}
