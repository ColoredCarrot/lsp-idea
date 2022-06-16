package info.voidev.lspidea.progress

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ex.ProgressIndicatorEx
import info.voidev.lspidea.connect.LspSession
import org.eclipse.lsp4j.WorkDoneProgressBegin
import org.eclipse.lsp4j.WorkDoneProgressEnd
import org.eclipse.lsp4j.WorkDoneProgressReport
import org.eclipse.lsp4j.jsonrpc.messages.Either

typealias LspProgressToken = Either<String, Int>

class LspProgressManager(private val session: LspSession) : Disposable {

    private val progresses = HashMap<LspProgressToken, LspProgress>()

    init {
        Disposer.register(session, this)
    }

    private fun interestedInProgress() = !session.project.isDisposed

    fun begin(token: LspProgressToken, params: WorkDoneProgressBegin) {
        if (!interestedInProgress()) {
            // If project is disposed,
            // we're not interested in any progress reporting
            return
        }

        val cancellable = params.cancellable ?: true
        val indeterminate = params.percentage == null || params.percentage < 0 || params.percentage > 100

        val indicator = LspProgressIndicator(
            session.project,
            params.title,
            null,
            null,
            cancellable,
            token,
            session.server
        )
        indicator.text = params.title
        indicator.text2 = params.message?.takeIf { it.isNotBlank() }
        indicator.isIndeterminate = indeterminate
        if (!indeterminate) indicator.fraction = params.percentage.toDouble() / 100.0

        indicator.start()

        progresses[token] = LspProgress(indicator)
    }

    fun report(token: LspProgressToken, params: WorkDoneProgressReport) {
        if (!interestedInProgress()) return

        val progress = progresses[token] ?: return logger.info("Got invalid progress token: $token")
        val indicator = progress.indicator

        // Update text2
        params.message?.also { newText2 ->
            indicator.text2 = newText2.takeIf { it.isNotBlank() }
        }

        // Update fraction
        val indeterminate = params.percentage == null || params.percentage < 0 || params.percentage > 100
        if (indeterminate) {
            if (!indicator.isIndeterminate) indicator.isIndeterminate = true
        } else {
            if (indicator.isIndeterminate) indicator.isIndeterminate = false
            indicator.fraction = params.percentage.toDouble() / 100.0
        }

        // Ignore params.cancellable because we always prefer the original cancellable value provided in begin()
    }

    fun end(token: LspProgressToken, params: WorkDoneProgressEnd) {
        if (!interestedInProgress()) return

        val progress = progresses.remove(token) ?: return logger.info("Got invalid progress token: $token")
        val indicator = progress.indicator

        params.message?.also { newText2 ->
            indicator.text2 = newText2.takeIf { it.isNotBlank() }
        }

        // First stop, then finished, see other usages of processFinish() in IntelliJ
        indicator.stop()
        (indicator as? ProgressIndicatorEx)?.processFinish()

        // We'll also re-highlight everything, because something may have changed
        DaemonCodeAnalyzer.getInstance(session.project).restart()
    }

    override fun dispose() {
        for (progress in progresses.values) {
            progress.indicator.stop()
        }
        progresses.clear()
    }

    companion object {
        private val logger = logger<LspProgressManager>()
    }
}
