package info.voidev.lspidea.connect

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import info.voidev.lspidea.LspIdea
import info.voidev.lspidea.util.runBackgroundable
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

object LspSessionDestructor {

    /**
     * You probably want to be calling [LspSession.destroy].
     *
     * Destroys the given session state by sending shutdown and exit signals as necessary.
     * After this method returns, the language server process is guaranteed to have exited.
     */
    fun destruct(state: LspSessionState) {
        destructAsyncWithProgress(state).join()
        state.serverOrNull = null
    }

    private fun destructAsyncWithProgress(state: LspSessionState): CompletableFuture<Unit> {
        return ProgressManager.getInstance()
            .runBackgroundable("Stopping language server", state.project, false) { indicator ->
                destructSync(state, indicator)
            }
    }

    fun destructSync(state: LspSessionState, indicator: ProgressIndicator) {
        ProgressManager.getInstance().executeProcessUnderProgress({
            ProgressManager.getInstance().executeNonCancelableSection {
                destructSync0(state, ProgressManager.getGlobalProgressIndicator()!!)
            }
        }, indicator)
    }

    private fun destructSync0(state: LspSessionState, indicator: ProgressIndicator) {
        indicator.text2 = "Stopping language server"

        if (state.status == LspStatus.STOPPED) {
            return
        }
        if (state.status == LspStatus.NOT_STARTED) {
            state.status = LspStatus.STOPPED
            return
        }

        //TODO: Find a good way to deal with concurrent session destruction.
        // Prolly best to have a ReadWriteLock for status (constructor/destructor write and methods that fire requests/notifications read)
//        if (state.status == LspStatus.STOPPING || state.status == LspStatus.FINALIZING) {
//            throw IllegalStateException("Attempting to destroy LSP session whose status is ${state.status}")
//        }

        // Check if we need to send the shutdown signal
        if (state.status == LspStatus.ACTIVE) {
            state.status = LspStatus.FINALIZING
            indicator.text2 = "Sending shutdown signal"
            doFinalize(state)
            state.status = LspStatus.FINALIZED
        }

        var exitDidTimeOut = false

        // Continue normally with sending exit signal
        if (state.status == LspStatus.FINALIZED) {
            state.status = LspStatus.STOPPING
            indicator.text2 = "Sending exit signal"

            state.server.exit()
            val exitCodeOrNull = state.process.awaitExit(EXIT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)

            exitDidTimeOut = exitCodeOrNull == null

            if (!exitDidTimeOut) {
                state.status = LspStatus.STOPPED
                if (exitCodeOrNull != 0) {
                    LspIdea.showWarning("Language server exited with non-zero exit code $exitCodeOrNull",
                        state.project)
                }
            }
        }

        if (state.status != LspStatus.STOPPED) {
            // Graceful exit failed; we must be forceful
            indicator.text2 = "Killing server"

            if (!exitDidTimeOut) {
                logger.error("Attempting to destroy LSP session whose status is ${state.status}")
            }

            logger.info("Failed to gracefully exit LSP server, killing it")
            state.process.kill()
        }

        state.status = LspStatus.STOPPED
    }

    /**
     * Sends the shutdown signal to the server
     * and waits for its response with a timeout.
     *
     * Never throws Exceptions.
     */
    private fun doFinalize(state: LspSessionState) {
        try {
            state.server.shutdown().get(SHUTDOWN_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
        } catch (ex: ExecutionException) {
            // Server failed to shut down; we'll tell them to exit anyway
            val cause = ex.cause
            if (cause is ResponseErrorException) {
                LspIdea.showResponseError("Graceful LSP shutdown failed", cause.responseError, state.project)
            } else {
                LspIdea.showError("Graceful LSP shutdown failed", (cause ?: ex).toString(), state.project)
                logger.error(ex)
            }
        } catch (_: TimeoutException) {
            LspIdea.showError("Graceful LSP shutdown timed out", state.project)
        } catch (ex: Exception) {
            LspIdea.showError("Graceful LSP shutdown failed", ex.toString(), state.project)
            logger.error(ex)
        }
    }

    private val logger = Logger.getInstance(javaClass)
    private const val SHUTDOWN_TIMEOUT_MILLIS = 5000L
    private const val EXIT_TIMEOUT_MILLIS = 5000L
}
