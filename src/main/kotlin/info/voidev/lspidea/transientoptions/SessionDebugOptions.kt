package info.voidev.lspidea.transientoptions

import com.intellij.openapi.util.Key

object SessionDebugOptions {
    @JvmStatic
    val DOCS_RAW_MARKDOWN = Key.create<Boolean>("rawMarkdown")

    @JvmStatic
    val DOCS_RAW_HTML = Key.create<Boolean>("rawHtml")
}
