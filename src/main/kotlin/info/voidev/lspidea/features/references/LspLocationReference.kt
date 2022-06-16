package info.voidev.lspidea.features.references

import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.light.LightElement
import com.intellij.util.IncorrectOperationException
import info.voidev.lspidea.dummy.LspDummyPsiElement
import info.voidev.lspidea.misc.LspFakeLanguage
import info.voidev.lspidea.util.lspRange2range
import org.eclipse.lsp4j.Location

class LspLocationReference(
    private val location: Location,
    document: Document,
    private val psiElement: LspDummyPsiElement,
) : PsiReference {

    private val range = document.lspRange2range(location.range)

    override fun getElement() = psiElement

    override fun getRangeInElement() = range

    override fun resolve() = object : LightElement(element.manager, LspFakeLanguage) {
        override fun toString() = "LSP fake PSI element"
    }

    override fun getCanonicalText() = "error"

    override fun handleElementRename(newElementName: String) = throw IncorrectOperationException()

    override fun bindToElement(element: PsiElement) = throw IncorrectOperationException()

    override fun isReferenceTo(element: PsiElement) = element is FakePsiElement

    override fun isSoft() = true

    private class FakePsiElement(manager: PsiManager) : LightElement(manager, LspFakeLanguage) {
        override fun toString(): String = javaClass.simpleName
    }
}
