package info.voidev.lspidea.connect

import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class LspLocalServerProcessHandler(private val builder: ProcessBuilder) : LspServerProcessHandler {

    private var process: Process? = null
    private var exitCode: Int? = null
    private var didRequestExitOrKill = false

    override fun start(): LspConnection = synchronized(this) {
        // If already started, perfect
        process?.also { return it.toConnection() }

        val process = try {
            builder.start()
        } catch (ex: Exception) {
            throw LspConnectionException("Failed to start local language server", ex)
        }

        if (!process.isAlive) {
            throw LspConnectionException("Failed to start local language server")
        }

        this.process = process
        return process.toConnection()
    }

    private fun Process.toConnection() = LspConnection(inputStream, outputStream, errorStream)

    override fun awaitExit(timeout: Long, unit: TimeUnit): Int? = synchronized(this) {
        val process = process ?: (return exitCode ?: 0)

        didRequestExitOrKill = true

        return if (process.waitFor(timeout, unit)) {
            exitCode = process.exitValue()
            exitCode
        } else {
            // Process did not exit in time
            null
        }
    }

    override fun kill() = synchronized(this) {
        didRequestExitOrKill = true
        process?.destroyForcibly()
        Unit
    }

    override fun onCrash(): CompletableFuture<Unit> {
        return process?.onExit()?.thenCompose { p ->
            if (didRequestExitOrKill) CompletableFuture()
            else CompletableFuture.completedFuture(Unit)
        } ?: throw IllegalStateException()
    }
}
