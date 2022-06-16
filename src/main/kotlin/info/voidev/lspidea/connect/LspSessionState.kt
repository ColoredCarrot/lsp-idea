package info.voidev.lspidea.connect

import com.intellij.openapi.project.Project
import info.voidev.lspidea.IdeaLanguageClient
import info.voidev.lspidea.def.ConfiguredLspServer
import info.voidev.lspidea.lspex.LanguageServerEx
import info.voidev.lspidea.lspex.debug.JrpcDebugger
import org.eclipse.lsp4j.ServerCapabilities
import org.eclipse.lsp4j.ServerInfo

class LspSessionState internal constructor(
    /**
     * Nullable because not every session state is necessarily
     * associated with a project
     * (however, a "live" session *is*).
     */
    val project: Project?,
    val serverDef: ConfiguredLspServer,
    val process: LspServerProcessHandler,
) {

    var status = LspStatus.NOT_STARTED

    var serverOrNull: LanguageServerEx? = null

    val client = IdeaLanguageClient(project)

    var serverInfoOrNull: ServerInfo? = null
    var serverCapabilitiesOrNull: ServerCapabilities? = null

    lateinit var debugger: JrpcDebugger

    val server: LanguageServerEx
        get() = serverOrNull!!

    val serverInfo: ServerInfo
        get() = serverInfoOrNull!!

    val serverCapabilities: ServerCapabilities
        get() = serverCapabilitiesOrNull!!
}
