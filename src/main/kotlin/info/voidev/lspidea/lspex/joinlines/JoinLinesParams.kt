package info.voidev.lspidea.lspex.joinlines

import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.TextDocumentIdentifier

open class JoinLinesParams constructor() {

    lateinit var textDocument: TextDocumentIdentifier

    lateinit var ranges: List<Range>

    constructor(textDocument: TextDocumentIdentifier, ranges: List<Range>) : this() {
        this.textDocument = textDocument
        this.ranges = ranges
    }

}
