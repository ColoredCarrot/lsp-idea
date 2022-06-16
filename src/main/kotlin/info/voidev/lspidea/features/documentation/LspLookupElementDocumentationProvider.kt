package info.voidev.lspidea.features.documentation

import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import info.voidev.lspidea.features.completion.CompletionItemWrapper
import org.eclipse.lsp4j.MarkupContent
import org.eclipse.lsp4j.MarkupKind

class LspLookupElementDocumentationProvider : AbstractLspDocumentationProvider() {

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        if (element !is LspLookupElementFakePsiElement) return null
        val documentation = element.completionItem.documentation ?: return null

        val markup =
            if (documentation.isLeft) MarkupContent(MarkupKind.PLAINTEXT, documentation.left)
            else documentation.right

        return translateMarkup(markup, element.session)
    }

    override fun getDocumentationElementForLookupItem(
        psiManager: PsiManager,
        `object`: Any,
        element: PsiElement,
    ): PsiElement? {
        if (`object` !is CompletionItemWrapper) return null

        return LspLookupElementFakePsiElement(`object`.session, `object`.completionItem, psiManager)
    }

    override fun getCustomDocumentationElement(
        editor: Editor,
        file: PsiFile,
        contextElement: PsiElement?,
        targetOffset: Int,
    ): PsiElement? {
        //TODO: Check if we actually "want to be used" in this file
        //TODO prolly send hover request to language server (?)
        return null
    }
}
