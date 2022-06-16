package info.voidev.lspidea.diagnostics

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import info.voidev.lspidea.LspSessionManager
import info.voidev.lspidea.util.LspUtils
import info.voidev.lspidea.util.identifyForLsp
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.PublishDiagnosticsParams

/**
 * Holds currently active diagnostics for a project
 */
class LspDiagnosticsManager(private val project: Project) {

    private val activeDiagnostics = HashMap<String, PublishDiagnosticsParams>()

    fun publishDiagnostics(diagnostics: PublishDiagnosticsParams) {
        // Normalize URI
        val uri = LspUtils.resolve(diagnostics.uri)?.identifyForLsp()?.uri
        if (uri == null) {
            logger<LspDiagnosticsManager>().warn("Invalid URI received from language server: ${diagnostics.uri}")
            return
        }
        activeDiagnostics[uri] = diagnostics
    }

    fun getDiagnostics(file: VirtualFile): List<Diagnostic> {
        val diagnostics = activeDiagnostics[file.identifyForLsp().uri] ?: return emptyList()

        val expectedVersion: Int? = diagnostics.version
        if (shouldCheckDocumentVersion && expectedVersion != null) {
            // We have to check the document version
            val actualVersion = LspSessionManager
                .getInstanceIfCreated(project)
                ?.getForFile(file)
                ?.openDocumentsManager
                ?.getIfOpen(file)
                ?.version

            if (actualVersion != expectedVersion) {
                return emptyList()
            }
        }

        return diagnostics.diagnostics
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project) = project.service<LspDiagnosticsManager>()

        private const val shouldCheckDocumentVersion = false
    }
}
