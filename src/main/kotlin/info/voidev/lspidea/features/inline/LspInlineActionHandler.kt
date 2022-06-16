package info.voidev.lspidea.features.inline

import com.intellij.lang.Language
import com.intellij.lang.refactoring.InlineActionHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import info.voidev.lspidea.dummy.LspDummyPsiElement
import info.voidev.lspidea.dummy.LspDummyPsiFile
import info.voidev.lspidea.dummy.LspElementTypes
import info.voidev.lspidea.features.refactor.LspInlineHandler
import info.voidev.lspidea.misc.LspFakeLanguage

class LspInlineActionHandler : InlineActionHandler() {

    override fun isEnabledForLanguage(l: Language?) =
        l == LspFakeLanguage

    override fun canInlineElement(element: PsiElement?) =
        element is LspDummyPsiElement ||
                element is LspDummyPsiFile ||
                element.elementType?.let { it == LspElementTypes.Content || it == LspElementTypes.File } == true

    override fun inlineElement(project: Project, editor: Editor?, element: PsiElement?) {
        LspInlineHandler.invoke(project, editor, element?.containingFile, null)
    }

}
