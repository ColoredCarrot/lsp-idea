package info.voidev.lspidea.lspex.moveitem

import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.TextDocumentIdentifier

open class MoveItemParams constructor() {

    lateinit var textDocument: TextDocumentIdentifier

    lateinit var range: Range

    /** @see Direction */
    lateinit var direction: String

    constructor(textDocument: TextDocumentIdentifier, range: Range, direction: String) : this() {
        this.textDocument = textDocument
        this.range = range
        this.direction = direction
    }

}
