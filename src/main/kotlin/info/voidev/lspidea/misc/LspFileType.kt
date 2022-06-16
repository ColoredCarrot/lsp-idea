package info.voidev.lspidea.misc

import com.intellij.icons.AllIcons
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.fileTypes.ex.FileTypeIdentifiableByVirtualFile
import com.intellij.openapi.project.ProjectLocator
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.impl.StubVirtualFile
import info.voidev.lspidea.def.LspServerDispatch
import javax.swing.Icon

object LspFileType : LanguageFileType(LspFakeLanguage), FileTypeIdentifiableByVirtualFile {

    override fun getName() = "LspFileType"

    override fun getDescription() = "File managed via language server over LSP"

    override fun getDefaultExtension() = "txt"

    override fun getIcon(): Icon? {
        return AllIcons.FileTypes.Any_type //TODO
    }

    override fun isMyFileType(file: VirtualFile): Boolean {
        if (file is StubVirtualFile) {
            //TODO will we ever support them?
            return false
        }
        return ProjectLocator.getInstance().getProjectsForFile(file).any { project ->
            !project.isDisposed && LspServerDispatch.getInstance(project).getForFile(file) != null
        }
    }
}
