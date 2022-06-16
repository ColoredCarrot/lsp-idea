package info.voidev.lspidea.features.declaration

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.jetbrains.rd.util.reflection.threadLocal
import info.voidev.lspidea.LspSessionManager
import info.voidev.lspidea.features.references.LocationLinkNavigable
import info.voidev.lspidea.features.references.LocationNavigable
import info.voidev.lspidea.util.identifyForLsp
import info.voidev.lspidea.util.joinUnwrapExceptionsCancellable
import info.voidev.lspidea.util.lspRange2range
import info.voidev.lspidea.util.mapToArray
import info.voidev.lspidea.util.offset2lspPosition
import org.eclipse.lsp4j.DeclarationParams
import org.eclipse.lsp4j.DefinitionParams
import org.eclipse.lsp4j.Location
import org.eclipse.lsp4j.LocationLink
import org.eclipse.lsp4j.ServerCapabilities

class LspGotoDeclarationHandler : GotoDeclarationHandler {

    override fun getGotoDeclarationTargets(
        sourceElement: PsiElement?,
        offset: Int,
        editor: Editor
    ): Array<PsiElement>? {
        return try {
            getGotoDeclarationTargets0(sourceElement, offset, editor)
        } finally {
//            currentOriginatingRange = null
        }
    }

    private fun getGotoDeclarationTargets0(
        sourceElement: PsiElement?,
        offset: Int,
        editor: Editor,
    ): Array<PsiElement>? {
        sourceElement ?: return null
        val project = editor.project ?: return null
        val file = FileDocumentManager.getInstance().getFile(editor.document) ?: return null
        val session = LspSessionManager.getInstance(project).getForFile(file) ?: return null

        // Use either the declaration or definition request,
        // depending on what the server supports,
        // and also a setting
        val resp = (
            if (!session.state.serverDef.preferGotoDefinition && session.state.serverCapabilities.supportsDeclaration())
                session.server.textDocumentService.declaration(
                    DeclarationParams(
                        file.identifyForLsp(),
                        editor.document.offset2lspPosition(offset)
                    )
                )
            else
                session.server.textDocumentService.definition(
                    DefinitionParams(
                        file.identifyForLsp(),
                        editor.document.offset2lspPosition(offset)
                    )
                )
            ).joinUnwrapExceptionsCancellable() ?: return null

        return if (resp.isLeft) {
            resp.left.mapToArray<Location, PsiElement> { location ->
                val originatingRange = guessOriginatingRange(editor.document, offset)
                currentOriginatingRange = originatingRange

                LspFakePsiElementForDeclaration(
                    navigable = LocationNavigable(project, location),
                    originatingRange = originatingRange,
                    manager = sourceElement.manager
                )
            }.ifEmpty { null }
        } else if (resp.isRight) {
            resp.right.mapToArray<LocationLink, PsiElement> { locationLink ->
                val originatingRange = (
                    locationLink.originSelectionRange?.let(editor.document::lspRange2range)
                        ?: guessOriginatingRange(editor.document, offset)
                    )
                currentOriginatingRange = originatingRange

                LspFakePsiElementForDeclaration(
                    navigable = LocationLinkNavigable(project, locationLink),
                    originatingRange = originatingRange,
                    manager = sourceElement.manager
                )
            }.ifEmpty { null }
        } else {
            null
        }
    }

    private fun ServerCapabilities.supportsDeclaration(): Boolean {
        val declarationProvider = declarationProvider ?: return false
        return declarationProvider.left ?: declarationProvider.isRight
    }

    /**
     * If the server doesn't provide us with an originating range
     * (i.e. the text range that will be underlined/highlighted blue on ctrl+hover),
     * we still need to guess one,
     * lest we highlight the entire file.
     */
    private fun guessOriginatingRange(document: Document, offset: Int): TextRange {
        // Limit our guess to 20 chars before and after offset
        val encompassingRange = TextRange(maxOf(offset - 20, 0), minOf(offset + 20, document.textLength))
        val offsetInEncompassing = offset - encompassingRange.startOffset
        val encompassingText = document.getText(encompassingRange)

        // Find word boundaries in encompassingText of the word at offset

        // First, explore backwards from offset...
        var wordBegInEncompassing = 0
        for (i in offsetInEncompassing - 1 downTo 0) {
            if (!isWordChar(encompassingText[i])) {
                wordBegInEncompassing = i + 1
                break
            }
        }

        // ...and then, explore forwards
        var wordEndInEncompassing = encompassingText.length
        for (i in offsetInEncompassing + 1 until encompassingText.length) {
            if (!isWordChar(encompassingText[i])) {
                wordEndInEncompassing = i
                break
            }
        }

        return TextRange(
            wordBegInEncompassing + encompassingRange.startOffset,
            wordEndInEncompassing + encompassingRange.endOffset
        )
    }

    private fun isWordChar(c: Char) = c in 'a'..'z' || c in 'A'..'Z' || c in '0'..'9' || c == '_'

    companion object {
        var currentOriginatingRange by threadLocal<TextRange?> { null }
    }
}
