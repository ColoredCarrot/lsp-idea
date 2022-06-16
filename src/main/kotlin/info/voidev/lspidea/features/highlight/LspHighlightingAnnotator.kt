package info.voidev.lspidea.features.highlight

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import info.voidev.lspidea.LspSessionManager
import info.voidev.lspidea.util.identifyForLsp

class LspHighlightingAnnotator : ExternalAnnotator<LspHighlightingContext, List<LspToken>>(), DumbAware {

    override fun collectInformation(file: PsiFile, editor: Editor, hasErrors: Boolean): LspHighlightingContext? {
        val vfile = file.virtualFile ?: return null
        val session = LspSessionManager.getInstance(file.project).getForFile(vfile) ?: return null

        return LspHighlightingContext(session, vfile.identifyForLsp(), editor.document)
    }

    override fun doAnnotate(collectedInfo: LspHighlightingContext?): List<LspToken>? {
        if (collectedInfo == null) return emptyList()

        return collectedInfo.session
            .semanticTokensManager
            .updateSemanticTokens(collectedInfo.document)
    }

    override fun apply(file: PsiFile, annotationResult: List<LspToken>?, holder: AnnotationHolder) {
        val clampRange = file.textRange
        annotationResult?.forEach { token ->
            val style = LspHighlightingMap.findStyle(token.type, token.mods)
            if (style != null) {
                holder
                    .newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(TextRange(token.offset, token.offset + token.length).intersection(clampRange)!!)
                    .textAttributes(style)
                    .create()
            }
        }
    }

}
