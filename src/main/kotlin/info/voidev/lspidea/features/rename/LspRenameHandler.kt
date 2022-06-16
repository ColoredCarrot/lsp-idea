package info.voidev.lspidea.features.rename

import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.rename.RenameHandler
import info.voidev.lspidea.LspSessionManager
import info.voidev.lspidea.util.identifyForLsp

class LspRenameHandler : RenameHandler {

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?, dataContext: DataContext?) {
        if (editor == null || file == null) return
        val vfile = FileDocumentManager.getInstance().getFile(editor.document) ?: return
        val session = LspSessionManager.getInstance(project).getForIfActive(vfile) ?: return

        LspRenameDialog(session, vfile.identifyForLsp(), project, editor).show()
    }

    override fun invoke(project: Project, elements: Array<out PsiElement>, dataContext: DataContext?) {
        // Not supported
        return
    }

    override fun isAvailableOnDataContext(dataContext: DataContext): Boolean {
        val project = dataContext.getData(CommonDataKeys.PROJECT) ?: return false
        val file = dataContext.getData(CommonDataKeys.VIRTUAL_FILE) ?: return false
        val session = LspSessionManager.getInstance(project).getForFile(file) ?: return false

        val renameProvider = session.state.serverCapabilities.renameProvider ?: return false
        return renameProvider.left != false
    }
}
