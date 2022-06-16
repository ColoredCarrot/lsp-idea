package info.voidev.lspidea.plugins.bundled.rustanalyzer

import icons.LspIcons
import info.voidev.lspidea.def.LspServer
import javax.swing.Icon

class RustAnalyzerLspServer : LspServer {

    override val displayName get() = "rust-analyzer"

    override val icon: Icon? get() = LspIcons.RUST_ANALYZER

    override val language get() = "Rust"

    override val documentationLinkSupport get() = RustDocumentationLinkSupport

}
