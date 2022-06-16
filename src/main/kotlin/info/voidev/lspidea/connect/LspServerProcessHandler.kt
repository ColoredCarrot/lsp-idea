package info.voidev.lspidea.connect

import java.util.concurrent.CompletionStage
import java.util.concurrent.TimeUnit

interface LspServerProcessHandler {

    fun start(): LspConnection

    /**
     * Waits for the process to exit and returns its exit code,
     * or null if the process fails to exit in the given timeframe.
     */
    fun awaitExit(timeout: Long, unit: TimeUnit): Int?

    fun kill()

    /**
     * Provides the ability to run code when the language server *crashes*.
     * Notably, the returned completion stage will never be executed
     * in case the server does not stop until a shutdown has been requested.
     */
    //TODO: Actually use this function somewhere to potentially auto-restart the server
    fun onCrash(): CompletionStage<Unit>

}
