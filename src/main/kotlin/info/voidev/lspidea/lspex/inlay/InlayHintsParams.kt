package info.voidev.lspidea.lspex.inlay

import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.TextDocumentIdentifier

open class InlayHintsParams constructor() {

    lateinit var textDocument: TextDocumentIdentifier

    var range: Range? = null

    constructor(textDocument: TextDocumentIdentifier, range: Range? = null) : this() {
        this.textDocument = textDocument
        this.range = range
    }
}
