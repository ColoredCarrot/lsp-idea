package info.voidev.lspidea.features.documentation.markdownrenderer

import com.vladsch.flexmark.util.data.NullableDataKey
import info.voidev.lspidea.def.LspDocumentationLinkSupport

object LspMdLinkExtension {
    val SUPPORT = NullableDataKey<LspDocumentationLinkSupport>("SUPPORT")
}
