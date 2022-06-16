package info.voidev.lspidea.features.documentation

import com.intellij.navigation.ItemPresentation
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.light.LightElement
import info.voidev.lspidea.connect.LspSession
import info.voidev.lspidea.misc.LspFakeLanguage
import info.voidev.lspidea.util.icon
import org.eclipse.lsp4j.CompletionItem

// see https://intellij-support.jetbrains.com/hc/en-us/community/posts/206124949-Documentation-for-LookupItems
class LspLookupElementFakePsiElement(
    val session: LspSession,
    val completionItem: CompletionItem,
    manager: PsiManager,
) : LightElement(manager, LspFakeLanguage), ItemPresentation {
    override fun toString(): String = javaClass.simpleName

    override fun getPresentation() = this

    override fun getPresentableText() = completionItem.label ?: completionItem.insertText ?: "<error>"

    override fun getIcon(unused: Boolean) = completionItem.kind.icon
}
