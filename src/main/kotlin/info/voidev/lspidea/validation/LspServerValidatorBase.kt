package info.voidev.lspidea.validation

import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressIndicator
import info.voidev.lspidea.connect.LspRunner
import info.voidev.lspidea.connect.LspSessionConstructor
import info.voidev.lspidea.connect.LspSessionDestructor
import info.voidev.lspidea.connect.LspSessionState
import info.voidev.lspidea.def.ConfiguredLspServer

open class LspServerValidatorBase : LspServerValidator {

    protected open fun sendSimpleRequests(sessionState: LspSessionState, progress: ProgressIndicator) {
    }

    override fun validate(
        server: ConfiguredLspServer,
        runner: LspRunner,
        progress: ProgressIndicator,
    ): LspServerValidationResult {

        // Try to establish a session and see if it's successful
        val sessionState = LspSessionState(null, server, runner)

        var exception: Exception? = null

        try {
            LspSessionConstructor.constructSync(sessionState, progress)
        } catch (ex: Exception) {
            // In the event of an exception, even if the user clicks cancel,
            // we still must destroy the session/dispose the resources
            exception = ex
        }

        if (exception == null) {
            try {
                sendSimpleRequests(sessionState, progress)
            } catch (ex: Exception) {
                exception = ex
            }
        }

        // Cannot throw (only Errors, which we're not interested in catching or handling gracefully)
        LspSessionDestructor.destructSync(sessionState, progress)

        return when (exception) {
            null -> LspServerValidationResult.Success(sessionState.serverInfoOrNull)
            is ProcessCanceledException -> LspServerValidationResult.Canceled
            else -> LspServerValidationResult.Failure(exception, sessionState.serverInfoOrNull)
        }
    }
}
