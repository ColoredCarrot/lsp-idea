package info.voidev.lspidea.lspex.moveitem

import org.eclipse.lsp4j.InsertTextFormat
import org.eclipse.lsp4j.TextEdit

open class SnippetTextEdit : TextEdit() {

    var insertTextFormat: InsertTextFormat? = null
}
