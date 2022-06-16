package info.voidev.lspidea.diagnostics

import org.eclipse.lsp4j.CodeAction
import org.eclipse.lsp4j.Command
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.jsonrpc.messages.Either

data class DiagnosticWithQuickFixes(val diagnostic: Diagnostic, val actions: List<Either<Command, CodeAction>>)
