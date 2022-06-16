package info.voidev.lspidea.diagnostics

import com.intellij.openapi.vfs.VirtualFile
import info.voidev.lspidea.connect.LspSession
import org.eclipse.lsp4j.Diagnostic

/**
 * Slim container for a single file's diagnostics
 * as fetched from the language server
 * as well as relevant contextual information.
 */
data class FileDiagnostics(val diagnostics: List<Diagnostic>, val session: LspSession, val file: VirtualFile)
