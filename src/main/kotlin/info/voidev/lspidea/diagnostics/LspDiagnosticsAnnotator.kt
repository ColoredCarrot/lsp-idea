package info.voidev.lspidea.diagnostics

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.xml.util.XmlStringUtil
import info.voidev.lspidea.LspSessionManager
import info.voidev.lspidea.features.codeaction.mayFetchCodeActions
import info.voidev.lspidea.util.asHighlightSeverity
import info.voidev.lspidea.util.getLineEndOffsetWrap
import info.voidev.lspidea.util.identifyForLsp
import info.voidev.lspidea.util.joinLsp
import info.voidev.lspidea.util.lspRange2range
import org.eclipse.lsp4j.CodeActionContext
import org.eclipse.lsp4j.CodeActionKind
import org.eclipse.lsp4j.CodeActionParams
import org.eclipse.lsp4j.DiagnosticTag

class LspDiagnosticsAnnotator : ExternalAnnotator<FileDiagnostics?, List<DiagnosticWithQuickFixes>>() {

    override fun collectInformation(file: PsiFile, editor: Editor, hasErrors: Boolean): FileDiagnostics? {
        val vfile = file.virtualFile ?: return null
        val session = LspSessionManager.getInstance(file.project).getForFile(file.virtualFile) ?: return null

        return FileDiagnostics(
            LspDiagnosticsManager.getInstance(file.project).getDiagnostics(vfile),
            session,
            vfile
        )
    }

    override fun doAnnotate(collectedInfo: FileDiagnostics?): List<DiagnosticWithQuickFixes> {
        if (collectedInfo == null) return emptyList()

        // If the server supports it, fetch code actions for each diagnostic
        // TODO can we make this just one fetch, specifying all diagnostics?
        return if (collectedInfo.session.state.serverCapabilities.mayFetchCodeActions) {
            collectedInfo.diagnostics.map { diagnostic ->

                val actions = collectedInfo.session.server.textDocumentService.codeAction(CodeActionParams(
                    collectedInfo.file.identifyForLsp(),
                    diagnostic.range,
                    CodeActionContext(listOf(diagnostic),
                        ALLOWED_CODE_ACTION_KINDS)
                )).joinLsp(collectedInfo.session.project, "Could not fetch diagnostics")

                DiagnosticWithQuickFixes(diagnostic, actions.orEmpty())
            }
        } else {
            collectedInfo.diagnostics.map { DiagnosticWithQuickFixes(it, emptyList()) }
        }
    }

    override fun apply(file: PsiFile, annotationResult: List<DiagnosticWithQuickFixes>, holder: AnnotationHolder) {
        val vfile = file.virtualFile ?: return
        val document = FileDocumentManager.getInstance().getDocument(vfile) ?: return

        for ((diagnostic, actions) in annotationResult) {

            val tags = diagnostic.tags.orEmpty()
            val range = document.lspRange2range(diagnostic.range)

            var builder = holder
                .newAnnotation(diagnostic.severity.asHighlightSeverity(), diagnostic.message)
                .tooltip(XmlStringUtil.wrapInHtmlLines(*diagnostic.message.lines().toTypedArray()))
                .range(range)

            if (range.isEmpty && range.startOffset == document.getLineEndOffsetWrap(diagnostic.range.start.line)) {
                builder = builder.afterEndOfLine()
            }

            if (DiagnosticTag.Deprecated in tags) {
                builder = builder.highlightType(ProblemHighlightType.LIKE_DEPRECATED)
            } else if (DiagnosticTag.Unnecessary in tags) {
                builder = builder.highlightType(ProblemHighlightType.LIKE_UNUSED_SYMBOL)
            }

            // TODO: handle Commands (i.e. it.left)
            builder = actions.asSequence()
                .mapNotNull { it.right }
                .filter { it.kind in ALLOWED_CODE_ACTION_KINDS }
                .fold(builder) { b, action ->
                    b.withFix(LspCodeActionIntentionAction(action))
                }

            //TODO: Add "Jump to..." intention to jump to any of org.eclipse.lsp4j.Diagnostic.relatedInformation

            builder.create()
        }
    }

    companion object {
        private val ALLOWED_CODE_ACTION_KINDS = listOf(CodeActionKind.QuickFix, CodeActionKind.SourceOrganizeImports)
    }
}
