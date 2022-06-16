package info.voidev.lspidea.def

interface LspDocumentationLinkSupport {

    fun matchLink(url: String): String?
}
