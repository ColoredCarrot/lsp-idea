package info.voidev.lspidea.plugins.bundled.rustanalyzer.config

import com.intellij.util.xmlb.annotations.Property
import info.voidev.lspidea.config.servers.LspServerOptionsConfigStateInterface
import info.voidev.lspidea.plugins.bundled.rustanalyzer.RustAnalyzerSupport
import info.voidev.lspidea.util.xml.Discriminator

@Discriminator(RustAnalyzerSupport.ID)
data class RustAnalyzerConfigState(
    @Property(surroundWithTag = false)
    var initOptions: RustAnalyzerInitOptions = RustAnalyzerInitOptions(),
) : LspServerOptionsConfigStateInterface
