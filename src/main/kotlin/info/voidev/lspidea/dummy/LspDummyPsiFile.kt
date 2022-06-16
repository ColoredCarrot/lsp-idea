package info.voidev.lspidea.dummy

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.light.LightElement
import info.voidev.lspidea.features.declaration.LspGotoDeclarationHandler
import info.voidev.lspidea.misc.LspFakeLanguage
import info.voidev.lspidea.misc.LspFileType

class LspDummyPsiFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, LspFakeLanguage) {
    override fun getFileType() = LspFileType

    val contentDummyPsi get() = findChildByClass(LspDummyPsiElement::class.java)

    override fun findElementAt(offset: Int): PsiElement? {
        val originatingRange = LspGotoDeclarationHandler.currentOriginatingRange
        if (originatingRange != null && offset in originatingRange && false) {//FIXME
            /*
            We inspect the stack trace to find out if we're currently in the process
            of a "goto declaration"
             */
            val isInGotoDeclaration = Throwable().stackTrace.any {
                it.className == "com.intellij.codeInsight.navigation.impl.GtdProvidersKt" && it.methodName == "fromGTDProvidersInner"
            }
            if (isInGotoDeclaration) {
                LspGotoDeclarationHandler.currentOriginatingRange = null
                return object : LightElement(manager, language) {
                    override fun toString() = "fake"

                    override fun getTextRange() = originatingRange
                }
            }
        }

        return super.findElementAt(offset)
    }
}
