package info.voidev.lspidea.config.servers

import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Property
import com.intellij.util.xmlb.annotations.Tag
import info.voidev.lspidea.config.serverhandler.LspRunnerConfigState
import info.voidev.lspidea.util.xml.UUIDConverter
import java.util.UUID

@Tag("server")
data class LspServerConfigState(
    @Attribute("id", converter = UUIDConverter::class)
    var id: UUID = UUID.randomUUID(),

    /**
     * The name given to this specific server instance by the user.
     */
    @Property
    @Tag("name")
    var name: String = "unnamed",

    @Property(surroundWithTag = false)
    var runner: LspRunnerConfigState = LspRunnerConfigState(),

    @Property(surroundWithTag = false)
    var options: LspServerOptionsConfigState = LspServerOptionsConfigState(),
)
