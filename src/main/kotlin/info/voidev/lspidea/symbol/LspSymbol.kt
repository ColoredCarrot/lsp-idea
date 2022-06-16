package info.voidev.lspidea.symbol

import com.intellij.navigation.ColoredItemPresentation
import com.intellij.navigation.ItemPresentation
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.editor.colors.CodeInsightColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import info.voidev.lspidea.connect.LspSession
import info.voidev.lspidea.util.LspUtils
import info.voidev.lspidea.util.icon
import org.eclipse.lsp4j.SymbolTag
import org.eclipse.lsp4j.WorkspaceSymbol

class LspSymbol(val session: LspSession, val info: WorkspaceSymbol) : NavigationItem {

    val isValid get() = session.isActive && file?.isValid != false

    override fun getName() = info.name!!

    val file by lazy(LazyThreadSafetyMode.PUBLICATION) {
        assert(session.isActive)
        LspUtils.resolve(info.location.left?.uri ?: info.location.right.uri)
    }

    private val fileDescriptor by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val file = file ?: return@lazy null
        info.location.left
            ?.range
            ?.start
            ?.let { pos -> OpenFileDescriptor(session.project, file, pos.line, pos.character) }
            ?: OpenFileDescriptor(session.project, file)
    }

    override fun getPresentation(): ItemPresentation = MyPresentation()

    override fun canNavigate() = canNavigateToSource()

    override fun canNavigateToSource(): Boolean {
        return isValid && file != null && fileDescriptor?.canNavigateToSource() == true
    }

    override fun navigate(requestFocus: Boolean) {
        if (isValid) {
            fileDescriptor?.navigateInEditor(session.project, requestFocus)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LspSymbol) return false

        if (info != other.info) return false

        return true
    }

    override fun hashCode(): Int {
        return info.hashCode()
    }

    private inner class MyPresentation : ColoredItemPresentation {
        private val style: TextAttributesKey?

        init {
            val isDeprecated = SymbolTag.Deprecated in info.tags.orEmpty()
            style = if (isDeprecated) CodeInsightColors.DEPRECATED_ATTRIBUTES else null
        }

        override fun getPresentableText() = name

        override fun getIcon(unused: Boolean) = info.kind.icon

        override fun getTextAttributesKey() = style
    }
}
