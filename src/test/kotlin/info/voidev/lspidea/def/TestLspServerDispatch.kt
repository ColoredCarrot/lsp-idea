package info.voidev.lspidea.def

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import info.voidev.lspidea.config.InstantiableLspServer

class TestLspServerDispatch(project: Project) : LspServerDispatch {
    override fun getForFile(file: VirtualFile): InstantiableLspServer? = null
}
