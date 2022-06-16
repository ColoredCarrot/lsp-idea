package info.voidev.lspidea.plugins.bundled.generic

import com.intellij.util.xmlb.annotations.Attribute
import info.voidev.lspidea.config.servers.LspServerOptionsConfigStateInterface
import info.voidev.lspidea.util.PathPattern
import info.voidev.lspidea.util.xml.Discriminator
import info.voidev.lspidea.util.xml.PathPatternConverter

@Discriminator(GenericLspServerSupport.ID)
data class GenericLspServerConfigState(
    @Attribute(converter = PathPatternConverter::class)
    val filenamePattern: PathPattern? = null,
) : LspServerOptionsConfigStateInterface
