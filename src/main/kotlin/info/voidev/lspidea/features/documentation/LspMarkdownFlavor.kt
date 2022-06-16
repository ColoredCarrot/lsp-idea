package info.voidev.lspidea.features.documentation

import org.intellij.markdown.IElementType
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.html.GeneratingProvider
import org.intellij.markdown.parser.LinkMap
import org.intellij.plugins.markdown.lang.parser.MarkdownParserManager
import java.net.URI

/**
 * @see MarkdownParserManager.FLAVOUR
 */
object LspMarkdownFlavor : GFMFlavourDescriptor() {
    override fun createHtmlGeneratingProviders(linkMap: LinkMap, baseURI: URI?): Map<IElementType, GeneratingProvider> {
        val providers = super.createHtmlGeneratingProviders(linkMap, baseURI)
        //TODO: Inject our own provider to transform links
        return providers
    }
}
