package info.voidev.lspidea.features.structureview

import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.pom.Navigatable
import info.voidev.lspidea.connect.LspSession
import info.voidev.lspidea.symbol.DocumentSymbolNavigable
import info.voidev.lspidea.symbol.DocumentSymbolPresentation
import info.voidev.lspidea.util.mapToArray
import org.eclipse.lsp4j.DocumentSymbol

class LspStructureViewHierarchicalElement(
    session: LspSession,
    file: VirtualFile,
    private val symbol: DocumentSymbol,
) :
    StructureViewTreeElement,
    SortableTreeElement,
    Navigatable by DocumentSymbolNavigable(session.project, file, symbol) {

    private val presentation = DocumentSymbolPresentation(symbol)

    private val children = symbol
        .children
        ?.mapToArray { LspStructureViewHierarchicalElement(session, file, it) }
        .orEmpty()

    override fun getPresentation() = presentation

    override fun getChildren() = children

    override fun getValue() = symbol

    override fun getAlphaSortKey(): String = symbol.name
}
