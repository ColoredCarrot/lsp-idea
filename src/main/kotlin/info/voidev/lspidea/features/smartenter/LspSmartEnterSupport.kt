package info.voidev.lspidea.features.smartenter

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.vfs.VirtualFile
import info.voidev.lspidea.connect.LspSession

interface LspSmartEnterSupport {

    /**
     * Attempt to perform smart enter.
     *
     * @return `true` iff smart enter was performed.
     */
    fun performSmartEnter(session: LspSession, file: VirtualFile, editor: Editor): Boolean
}
