package info.voidev.lspidea.symbol

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import info.voidev.lspidea.LspSessionManager
import info.voidev.lspidea.connect.LspSession
import info.voidev.lspidea.util.joinUnwrapExceptionsCancellable
import org.eclipse.lsp4j.SymbolInformation
import org.eclipse.lsp4j.WorkspaceSymbol
import org.eclipse.lsp4j.WorkspaceSymbolParams
import org.eclipse.lsp4j.jsonrpc.messages.Either

object LspSymbolProvider {

    fun get(query: String?, project: Project?): Sequence<LspSymbol> {
        return fetch(project, WorkspaceSymbolParams(query?.trim().orEmpty()))
    }

    @JvmName("fetchNullableProject")
    private fun fetch(project: Project?, params: WorkspaceSymbolParams): Sequence<LspSymbol> {
        return if (project != null) {
            fetch(project, params)
        } else {
            ProjectManager.getInstance().openProjects.asSequence()
                .flatMap { p -> fetch(p, params) }
        }
    }

    private fun fetch(project: Project, params: WorkspaceSymbolParams): Sequence<LspSymbol> {
        return LspSessionManager.getInstance(project).getAll().asSequence()
            .flatMap { session -> fetch(session, params) }
    }

    private fun fetch(session: LspSession, params: WorkspaceSymbolParams): Sequence<LspSymbol> {
        val result: Either<List<SymbolInformation>, List<WorkspaceSymbol>> = session
            .server
            .workspaceService
            ?.symbol(params)
            ?.joinUnwrapExceptionsCancellable()
            ?: return emptySequence()

        return if (result.isLeft) {
            result.left.asSequence()
                .map { LspSymbol(session, it.adapt()) }
        } else {
            result.right.asSequence()
                .map { LspSymbol(session, it) }
        }
    }

}
