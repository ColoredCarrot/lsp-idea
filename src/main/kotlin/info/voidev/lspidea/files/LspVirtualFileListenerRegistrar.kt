package info.voidev.lspidea.files

import com.intellij.openapi.vfs.VirtualFileManager
import info.voidev.lspidea.connect.LspSession
import info.voidev.lspidea.event.LspSessionListener

class LspVirtualFileListenerRegistrar : LspSessionListener {
    override fun newSession(session: LspSession) {
        // Register file listener for session
        session.project.messageBus.connect(session).subscribe(VirtualFileManager.VFS_CHANGES, LspVirtualFileListener(session))
    }
}
