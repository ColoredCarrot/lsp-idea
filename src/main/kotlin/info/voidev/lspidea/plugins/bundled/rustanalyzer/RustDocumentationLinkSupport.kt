package info.voidev.lspidea.plugins.bundled.rustanalyzer

import info.voidev.lspidea.def.LspDocumentationLinkSupport
import org.intellij.lang.annotations.Language

object RustDocumentationLinkSupport : LspDocumentationLinkSupport {

    override fun matchLink(url: String): String? {
        // Rust documentation links look like:  `std::print!`
        if (!(url matches regex)) return null

        return "data:text/html,<html><body>Not%20yet%20implemented</body></html>"
    }

    private val regex: Regex

    init {
        @Language("RegExp")
        val baseRegex = "(::)?\\w+(::\\w+)*!?"
        regex = Regex("$baseRegex|`$baseRegex`")
    }
}
