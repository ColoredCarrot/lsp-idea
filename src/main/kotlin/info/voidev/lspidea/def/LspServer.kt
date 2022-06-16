package info.voidev.lspidea.def

import info.voidev.lspidea.features.smartenter.LspSmartEnterSupport
import org.jetbrains.annotations.Nls
import javax.swing.Icon

/**
 * Encapsulates information about a specific language server.
 *
 * One support plugin should provide exactly one implementation of this interface
 * per language server.
 * For example, if your plugin provides support for rust-analyzer,
 * you should provide one `RustAnalyzerLspServer : LspServer`.
 */
interface LspServer {

    /**
     * Static display name used when there is no active session
     * to query the server's advertised name from
     * and the user hasn't renamed the server.
     */
    val displayName: @Nls String

    val icon: Icon? get() = null

    /**
     * The name of the programming language or other text kind
     * supported by this server, e.g. "Kotlin" or "C++".
     *
     * Visible to the user.
     */
    val language: @Nls String

    val smartEnterSupport: LspSmartEnterSupport? get() = null
    val documentationLinkSupport: LspDocumentationLinkSupport? get() = null
    val preferGotoDefinition: Boolean get() = true

}
