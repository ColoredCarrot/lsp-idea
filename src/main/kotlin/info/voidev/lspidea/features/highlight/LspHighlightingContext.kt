package info.voidev.lspidea.features.highlight

import com.intellij.openapi.editor.Document
import info.voidev.lspidea.connect.LspSession
import org.eclipse.lsp4j.TextDocumentIdentifier

class LspHighlightingContext(
    val session: LspSession,
    val file: TextDocumentIdentifier,
    val document: Document,
)
