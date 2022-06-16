package info.voidev.lspidea.features.documentation

import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import info.voidev.lspidea.LspIdea
import info.voidev.lspidea.LspSessionManager
import info.voidev.lspidea.dummy.LspDummyPsiFile
import info.voidev.lspidea.util.identifyForLsp
import info.voidev.lspidea.util.joinLsp
import info.voidev.lspidea.util.offset2lspPosition
import org.eclipse.lsp4j.HoverParams

class LspDocumentationProvider : AbstractLspDocumentationProvider() {

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        if (element !is LspFakePsiElementForDocumentation) return null

        val documentation = element.info.contents
        val documentationContent = documentation.right ?: convertMarkedStrings(documentation.left)

        if (documentation.isLeft) {
            LspIdea.showWarning(
                "Outdated language server: ${element.session.state.serverInfo.name} is still using MarkedString instead of MarkupContent",
                element.project
            )
        }

        return translateMarkup(documentationContent, element.session)
    }

    override fun getCustomDocumentationElement(
        editor: Editor,
        file: PsiFile,
        contextElement: PsiElement?,
        targetOffset: Int,
    ): PsiElement? {
        if (file !is LspDummyPsiFile) return null

        val project = file.project
        val vfile = file.virtualFile ?: return null

        val targetPosition = editor.document.offset2lspPosition(targetOffset)

        val session = LspSessionManager.getInstance(project).getForFile(vfile) ?: return null

        val info = session.server.textDocumentService.hover(
            HoverParams(
                vfile.identifyForLsp(),
                targetPosition
            )
        ).joinLsp(project, "Could not fetch documentation")
            ?: return null

        return LspFakePsiElementForDocumentation(session, vfile, targetOffset, targetPosition, info, file.manager)
    }

    override fun getDocumentationElementForLink(
        psiManager: PsiManager?,
        link: String?,
        context: PsiElement?,
    ): PsiElement? {
        // TODO (also read javadoc of this method)
        return null
    }
}
