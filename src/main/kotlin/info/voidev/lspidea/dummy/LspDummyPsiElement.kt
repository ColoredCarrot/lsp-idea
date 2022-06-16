package info.voidev.lspidea.dummy

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.model.psi.PsiExternalReferenceHost
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry

class LspDummyPsiElement(node: ASTNode) : ASTWrapperPsiElement(node), PsiExternalReferenceHost {

    override fun getReferences(): Array<PsiReference> {
        return ReferenceProvidersRegistry.getReferencesFromProviders(this)
    }

}
