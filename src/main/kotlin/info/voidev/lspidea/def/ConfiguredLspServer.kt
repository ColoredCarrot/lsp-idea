package info.voidev.lspidea.def

import com.intellij.openapi.vfs.VirtualFile

// TODO: Prefer composition over inheritance here
sealed interface ConfiguredLspServer : LspServer {

    val initOptions: Any?

    /**
     * Checks whether a session for this language server should be spawned
     * automatically for a given [file].
     */
    fun shouldActivateForFile(file: VirtualFile): Boolean

}

abstract class AbstractConfiguredLspServer(private val server: LspServer) : ConfiguredLspServer, LspServer by server
