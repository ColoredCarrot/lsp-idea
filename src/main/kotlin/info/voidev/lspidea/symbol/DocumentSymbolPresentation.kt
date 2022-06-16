package info.voidev.lspidea.symbol

import com.intellij.navigation.ColoredItemPresentation
import com.intellij.openapi.editor.colors.CodeInsightColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import info.voidev.lspidea.util.icon
import org.eclipse.lsp4j.DocumentSymbol
import org.eclipse.lsp4j.SymbolTag

class DocumentSymbolPresentation(private val symbol: DocumentSymbol) : ColoredItemPresentation {

    private val style: TextAttributesKey?

    init {
        val isDeprecated = SymbolTag.Deprecated in symbol.tags.orEmpty() || symbol.deprecated == true
        style = if (isDeprecated) CodeInsightColors.DEPRECATED_ATTRIBUTES else null
    }

    // TODO also display symbol.detail
    override fun getPresentableText(): String = symbol.name

    override fun getIcon(unused: Boolean) = symbol.kind.icon

    override fun getTextAttributesKey() = style
}
