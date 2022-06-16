package info.voidev.lspidea.features.declaration

import com.intellij.openapi.util.TextRange
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.light.LightElement
import info.voidev.lspidea.misc.LspFakeLanguage

class LspFakePsiElementForDeclaration(
    private val navigable: Navigatable,
    private val originatingRange: TextRange,//FIXME HOW TF DO WE ACTUALLY TELL INTELLIJ ABOUT THIS
    manager: PsiManager,
) : LightElement(manager, LspFakeLanguage) {

    override fun toString(): String = javaClass.simpleName

    override fun canNavigate() = navigable.canNavigate()

    override fun canNavigateToSource() = navigable.canNavigateToSource()

    override fun navigate(requestFocus: Boolean) = navigable.navigate(requestFocus)

}
