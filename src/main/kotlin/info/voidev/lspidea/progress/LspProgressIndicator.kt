package info.voidev.lspidea.progress

import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.project.Project
import org.eclipse.lsp4j.WorkDoneProgressCancelParams
import org.eclipse.lsp4j.services.LanguageServer

class LspProgressIndicator(
    project: Project,
    progressTitle: String,
    cancelButtonText: String?,
    backgroundStopTooltip: String?,
    cancellable: Boolean,
    private val token: LspProgressToken,
    private val server: LanguageServer
) : BackgroundableProcessIndicator(project, progressTitle, cancelButtonText, backgroundStopTooltip, cancellable) {

    override fun onRunningChange() {
        super.onRunningChange()
        if (isCanceled && isCancelable) {
            handleCancelled()
        }
    }

    private fun handleCancelled() {
        server.cancelProgress(WorkDoneProgressCancelParams(token))
    }

}
