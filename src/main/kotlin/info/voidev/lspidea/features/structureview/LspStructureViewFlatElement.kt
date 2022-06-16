package info.voidev.lspidea.features.structureview

import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.NavigationItem
import info.voidev.lspidea.symbol.LspSymbol

class LspStructureViewFlatElement(
    private val symbol: LspSymbol,
) : StructureViewTreeElement, SortableTreeElement, NavigationItem by symbol {

    override fun getPresentation() = symbol.presentation

    override fun getChildren(): Array<TreeElement> = emptyArray()

    override fun getValue() = symbol

    override fun getAlphaSortKey() = symbol.name

}
