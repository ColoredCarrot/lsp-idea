package info.voidev.lspidea.config.runner

import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Tag

@Tag("localProcess")
data class LocalProcessLspRunnerConfigState(
    @Attribute
    var executablePath: String = "",
) : LspRunnerConfigStateInterface
