package info.voidev.lspidea.misc

import org.eclipse.lsp4j.CodeAction
import org.eclipse.lsp4j.Command

fun Command.toCodeAction() = CodeAction(title).also {
    it.command = this
}
