package info.voidev.lspidea.features.documentation.markdownrenderer

import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet
import info.voidev.lspidea.connect.LspSession

class LspMarkdownRenderer(session: LspSession) {

    private val parser: Parser

    private val renderer: HtmlRenderer

    init {
        val options = MutableDataSet()

        options[HtmlRenderer.ESCAPE_HTML] = true

        session.state.serverDef.documentationLinkSupport?.also {
            options[LspMdLinkExtension.SUPPORT] = it
        }

        parser = Parser.builder(options)
            .build()
        renderer = HtmlRenderer.builder(options)
            .linkResolverFactory(LspMdLinkResolver.Factory)
            .build()
    }

    fun render(markdown: String): String {
        return renderer.render(parser.parse(markdown))
    }
}
