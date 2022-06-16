package info.voidev.lspidea.features.documentation.markdownrenderer

import com.vladsch.flexmark.html.LinkResolver
import com.vladsch.flexmark.html.LinkResolverFactory
import com.vladsch.flexmark.html.renderer.LinkResolverBasicContext
import com.vladsch.flexmark.html.renderer.LinkStatus
import com.vladsch.flexmark.html.renderer.LinkType
import com.vladsch.flexmark.html.renderer.ResolvedLink
import com.vladsch.flexmark.util.ast.Node
import info.voidev.lspidea.def.LspDocumentationLinkSupport

class LspMdLinkResolver(private val support: LspDocumentationLinkSupport?) : LinkResolver {

    override fun resolveLink(node: Node, ctx: LinkResolverBasicContext, link: ResolvedLink): ResolvedLink {
        if (support == null || link.linkType != LinkType.LINK) {
            return link
        }

        val newUrl = support.matchLink(link.url)
        if (newUrl != null) {
            return ResolvedLink(LinkType.LINK, newUrl, null, LinkStatus.UNCHECKED)
        }

        return link
    }

    object Factory : LinkResolverFactory {
        override fun getBeforeDependents(): MutableSet<Class<*>>? = null

        override fun getAfterDependents(): MutableSet<Class<*>>? = null

        override fun affectsGlobalScope() = false

        override fun apply(ctx: LinkResolverBasicContext): LspMdLinkResolver {
            val support = LspMdLinkExtension.SUPPORT.get(ctx.options)
            return LspMdLinkResolver(support)
        }
    }
}
