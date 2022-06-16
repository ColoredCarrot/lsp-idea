package info.voidev.lspidea.features.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiElement
import info.voidev.lspidea.LspSessionManager
import info.voidev.lspidea.util.contains
import info.voidev.lspidea.util.identifyForLsp
import info.voidev.lspidea.util.joinLsp
import info.voidev.lspidea.util.lspPosition2offset
import info.voidev.lspidea.util.range2lspRange
import org.eclipse.lsp4j.FoldingRange
import org.eclipse.lsp4j.FoldingRangeKind
import org.eclipse.lsp4j.FoldingRangeRequestParams
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range

class LspFoldingBuilder : FoldingBuilderEx() {

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        if (quick) return emptyArray()

        val session = LspSessionManager.getInstance(root.project).getFor(document) ?: return emptyArray()

        if (!LspFoldingFeature.isAvailable(session)) return emptyArray()

        val ranges = session.server.textDocumentService.foldingRange(FoldingRangeRequestParams(
            document.identifyForLsp()
        )).joinLsp(session.project, "Could not fetch folding ranges")
            ?: return emptyArray()

        val getInRange = document.range2lspRange(root.textRange)

        return ranges.asSequence()
            .filter {
                val startPos = Position(it.startLine, it.startCharacter ?: 0)
                val endPos = Position(it.endLine, it.endCharacter ?: document.getLineEndOffset(it.endLine))
                Range(startPos, endPos) in getInRange
            }
            .map { range ->
                FoldingDescriptor(
                    root,
                    document.lspPosition2offset(range.startLine, range.startCharacter ?: 0),
                    document.lspPosition2offset(range.endLine, range.endCharacter ?: document.getLineEndOffset(range.endLine)),
                    null,
                    getPlaceholderText(range)
                //TODO: if range.kind == Imports, then collapsedByDefault
                )
            }
            .toList().toTypedArray()
    }

    private fun getPlaceholderText(range: FoldingRange): String {
        // Prefer the given placeholder text if provided
        range.collapsedText?.also { return it }

        return when (range.kind) {
            FoldingRangeKind.Comment -> "Comment"
            FoldingRangeKind.Imports -> "Imports"
            FoldingRangeKind.Region -> "Region"
            else -> "..."
        }
    }

    override fun getPlaceholderText(node: ASTNode) =
        throw AssertionError("Placeholder texts are supplied during construction")

    override fun isCollapsedByDefault(node: ASTNode) = false

}
