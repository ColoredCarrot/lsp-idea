package info.voidev.lspidea.features.structureview

import com.intellij.ide.structureView.StructureViewBuilder
import com.intellij.ide.structureView.StructureViewModel
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder
import com.intellij.lang.PsiStructureViewFactory
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import info.voidev.lspidea.LspIdea
import info.voidev.lspidea.LspSessionManager
import info.voidev.lspidea.connect.LspSession
import info.voidev.lspidea.dummy.LspDummyPsiFile
import info.voidev.lspidea.symbol.LspSymbol
import info.voidev.lspidea.symbol.adapt
import info.voidev.lspidea.util.identifyForLsp
import info.voidev.lspidea.util.joinUnwrapExceptionsCancellable
import info.voidev.lspidea.util.mapToArray
import org.eclipse.lsp4j.DocumentSymbol
import org.eclipse.lsp4j.DocumentSymbolParams
import org.eclipse.lsp4j.WorkspaceSymbol

class LspStructureViewFactory : PsiStructureViewFactory {

    // See https://microsoft.github.io/language-server-protocol/specifications/specification-current/#textDocument_documentSymbol

    override fun getStructureViewBuilder(psiFile: PsiFile): StructureViewBuilder? {
        if (psiFile !is LspDummyPsiFile) return null
        val file = psiFile.virtualFile ?: return null

        val session = LspSessionManager.getInstance(psiFile.project).getForFile(file) ?: return null
        if (!session.isActive) return null

        return object : TreeBasedStructureViewBuilder() {
            override fun createStructureViewModel(editor: Editor?): StructureViewModel {
                return LspStructureViewModel(psiFile, editor, createRoot(session, file))
            }
        }
    }

    private fun createRoot(session: LspSession, file: VirtualFile): StructureViewTreeElement {
        val symbols = session.server.textDocumentService.documentSymbol(DocumentSymbolParams(file.identifyForLsp()))
            .joinUnwrapExceptionsCancellable()

        if (symbols.isEmpty()) {
            return LspStructureViewFileElement(session, file, emptyArray())
        }

        val aSymbol = symbols.first()
        if (aSymbol.isLeft) {
            val flatSymbols = symbols.mapNotNull {
                val symbol = it.left
                if (symbol == null) {
                    LspIdea.showError("Language server is broken", "Received mixed document symbols", session.project)
                }
                symbol.adapt()
            }
            return createRootFlat(session, file, flatSymbols)
        } else {
            val hierarchicalSymbols = symbols.mapNotNull {
                val symbol = it.right
                if (symbol == null) {
                    LspIdea.showError("Language server is broken", "Received mixed document symbols", session.project)
                }
                symbol
            }
            return createRootHierarchical(session, file, hierarchicalSymbols)
        }
    }

    // Note: For rust-analyzer, both flat and hierarchical are tested
    // (rust-analyzer sends one or the other depending on the client capabilities we specify)

    private fun createRootFlat(
        session: LspSession,
        file: VirtualFile,
        symbols: List<WorkspaceSymbol>,
    ): StructureViewTreeElement {
        symbols.singleOrNull()?.also { singleSymbol ->
            return LspStructureViewFlatElement(LspSymbol(session, singleSymbol))
        }

        return LspStructureViewFileElement(session, file, symbols.mapToArray { symbol ->
            LspStructureViewFlatElement(LspSymbol(session, symbol))
        })
    }

    private fun createRootHierarchical(
        session: LspSession,
        file: VirtualFile,
        symbols: List<DocumentSymbol>,
    ): StructureViewTreeElement {
        symbols.singleOrNull()?.also { singleSymbol ->
            return LspStructureViewHierarchicalElement(session, file, singleSymbol)
        }

        return LspStructureViewFileElement(session, file, symbols.mapToArray { symbol ->
            LspStructureViewHierarchicalElement(session, file, symbol)
        })
    }
}
