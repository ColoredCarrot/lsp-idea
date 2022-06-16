package info.voidev.lspidea.features.refactor

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsContexts.DialogMessage
import com.intellij.openapi.util.NlsContexts.DialogTitle
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.RefactoringActionHandler
import com.intellij.refactoring.RefactoringBundle
import com.intellij.refactoring.util.CommonRefactoringUtil
import info.voidev.lspidea.LspSessionManager
import info.voidev.lspidea.connect.LspSession
import info.voidev.lspidea.dummy.LspDummyPsiFile

abstract class AbstractLspRefactoringHandler(
    @DialogTitle protected open val refactoringName: String,
) : RefactoringActionHandler {

    protected abstract fun perform(session: LspSession, editor: Editor, file: VirtualFile)

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?, dataContext: DataContext?) {
        if (editor == null || file == null) {
            return showCannotRefactorHint(project, editor)
        }

        if (file !is LspDummyPsiFile) {
            return
        }

        val vfile = file.virtualFile
            ?: return showCannotRefactorHint(project, editor)

        val session = LspSessionManager.getInstance(project).getForFile(vfile)
            ?: return showErrorHint("No LSP session found for file", editor, project)

        perform(session, editor, vfile)
    }

    override fun invoke(project: Project, elements: Array<out PsiElement>, dataContext: DataContext?) {
        showCannotRefactorHint(project, null)
    }

    protected open fun showErrorHint(errorMessage: @DialogMessage String, editor: Editor?, project: Project) {
        CommonRefactoringUtil.showErrorHint(project, editor, errorMessage, refactoringName, null)
    }

    protected open fun showCannotRefactorHint(project: Project, editor: Editor?) {
        showErrorHint(RefactoringBundle.message("cannot.perform.refactoring"), editor, project)
    }

}
