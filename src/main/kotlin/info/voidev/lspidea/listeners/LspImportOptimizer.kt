package info.voidev.lspidea.listeners

import com.intellij.lang.ImportOptimizer
import com.intellij.psi.PsiFile
import info.voidev.lspidea.LspSessionManager
import org.eclipse.lsp4j.CodeActionKind

class LspImportOptimizer : ImportOptimizer {

    override fun supports(file: PsiFile): Boolean {
        val vfile = file.virtualFile
        val session = LspSessionManager.getInstance(file.project).getForFile(vfile) ?: return false

        return session.state.serverCapabilities
            .codeActionProvider
            ?.right
            ?.codeActionKinds
            ?.contains(CodeActionKind.SourceOrganizeImports) == true
    }

    override fun processFile(file: PsiFile) = Runnable {
        // TODO implement later, currently rust-analyzer doesn't support this: see https://github.com/rust-analyzer/rust-analyzer/issues/5131
    }
}
