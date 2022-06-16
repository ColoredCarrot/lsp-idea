package info.voidev.lspidea.features.documentation

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.documentation.DocumentationMarkup
import info.voidev.lspidea.connect.LspSession
import info.voidev.lspidea.features.documentation.markdownrenderer.LspMarkdownRenderer
import org.apache.commons.lang.StringEscapeUtils
import org.eclipse.lsp4j.MarkedString
import org.eclipse.lsp4j.MarkupContent
import org.eclipse.lsp4j.MarkupKind
import org.eclipse.lsp4j.jsonrpc.messages.Either

abstract class AbstractLspDocumentationProvider : AbstractDocumentationProvider() {

    protected fun convertMarkedStrings(parts: List<Either<String, MarkedString>>): MarkupContent {
        return MarkupContent(
            MarkupKind.MARKDOWN,
            parts.joinToString("\n\n") { part ->
                if (part.isLeft) {
                    part.left
                } else {
                    "```${part.right.language}\n${part.right.value}\n```"
                }
            }
        )
    }

    protected fun translateMarkup(
        markup: MarkupContent,
        session: LspSession,
    ): String? = when (markup.kind) {
        MarkupKind.PLAINTEXT -> DocumentationMarkup.CONTENT_START + markup.value + DocumentationMarkup.CONTENT_END
        MarkupKind.MARKDOWN -> markdown2html(markup.value, session)
        else -> null
    }

    private fun markdown2html(markdown: String, session: LspSession): String {
        if (DEBUG_PRINT_RAW_MARKDOWN) {
            var html = StringEscapeUtils.escapeHtml(markdown)
            html = "<strong>Note: Raw markdown is shown as a debugging feature.</strong><br/><br/>$html"
            return html
        }

        var html = LspMarkdownRenderer(session).render(markdown)

        if (DEBUG_PRINT_RAW_HTML) {
            html = StringEscapeUtils.escapeHtml(html)
            html = "<strong>Note: HTML is escaped as a debugging feature.</strong><br/><br/>$html"
        }

        return html
    }

    companion object {
        private const val DEBUG_PRINT_RAW_MARKDOWN = false
        private const val DEBUG_PRINT_RAW_HTML = false
    }
}
