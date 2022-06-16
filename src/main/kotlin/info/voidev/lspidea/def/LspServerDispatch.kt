package info.voidev.lspidea.def

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import info.voidev.lspidea.config.InstantiableLspServer

interface LspServerDispatch {

    /**
     * This method must be thread-safe and fast enough to run on the EDT.
     */
    fun getForFile(file: VirtualFile): InstantiableLspServer?

    companion object {
        fun getInstance(project: Project) = project.service<LspServerDispatch>()
    }
}
