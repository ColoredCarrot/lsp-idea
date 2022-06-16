package info.voidev.lspidea.features.codelens

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.icons.AllIcons
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import info.voidev.lspidea.LspSessionManager
import info.voidev.lspidea.util.identifyForLsp
import info.voidev.lspidea.util.joinLsp
import info.voidev.lspidea.util.lspRange2range
import org.eclipse.lsp4j.CodeLens
import org.eclipse.lsp4j.CodeLensParams
import javax.swing.Icon

class LspCodeLensLineMarkerProvider : LineMarkerProvider {
    // Would com.intellij.execution.lineMarker.RunLineMarkerProvider work?

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        // Always null, since the entire file consists of only one PsiElement
        // and we need to support multiple code lenses per file
        return null
    }

    override fun collectSlowLineMarkers(
        elements: List<PsiElement>,
        result: MutableCollection<in LineMarkerInfo<*>>,
    ) {
        if (elements.isEmpty()) return
        val psiFile = elements.first().containingFile ?: return

        // Always called for only one PsiFile
        logger.assertTrue(elements.all { it.containingFile == psiFile })

        val file = psiFile.virtualFile ?: return
        run(psiFile.project, file, elements.first(), result)
    }

    private fun run(
        project: Project,
        file: VirtualFile,
        element: PsiElement,
        result: MutableCollection<in LineMarkerInfo<*>>,
    ) {
        val session = LspSessionManager.getInstance(project).getForFile(file) ?: return
        val document = FileDocumentManager.getInstance().getDocument(file) ?: return

        if (!CodeLensLspFeature.isAvailable(session)) {
            return
        }

        val codeLenses: List<CodeLens>? = session.server.textDocumentService
            .codeLens(CodeLensParams(file.identifyForLsp()))
            .joinLsp(project, "Could not fetch code lenses")

        if (codeLenses.isNullOrEmpty()) return

        for (codeLens in codeLenses) {
            val icon = getIconForCodeLens(codeLens) ?: continue
            result += LspCodeLensItem(
                session,
                AutoCodeLens(codeLens, session),
                element,
                document.lspRange2range(codeLens.range),
                icon
            )
        }
    }

    private fun getIconForCodeLens(codeLens: CodeLens): Icon? {
        // TODO: More fancy stuff
        val commandTitle = codeLens.command?.title ?: return AllIcons.Nodes.Tag
        return when {
            commandTitle.contains("debug", ignoreCase = true) -> AllIcons.Actions.StartDebugger
            commandTitle.contains("run", ignoreCase = true) -> AllIcons.Actions.Execute
            else -> AllIcons.Nodes.Tag
        }
    }

    companion object {
        private val logger = logger<LspCodeLensLineMarkerProvider>()
    }
}
