package info.voidev.lspidea.connect

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import info.voidev.lspidea.LspIdea
import info.voidev.lspidea.lsp.LspIdeaCapabilities
import info.voidev.lspidea.lspex.LSPLauncherEx
import info.voidev.lspidea.util.joinUnwrapExceptionsCancellable
import info.voidev.lspidea.util.runBackgroundable
import org.eclipse.lsp4j.ClientInfo
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.InitializeResult
import org.eclipse.lsp4j.InitializedParams
import org.eclipse.lsp4j.ServerInfo
import org.eclipse.lsp4j.TextDocumentSyncKind
import org.eclipse.lsp4j.TextDocumentSyncOptions
import org.eclipse.lsp4j.TraceValue
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.jetbrains.annotations.Nls
import java.nio.file.Path
import java.util.Locale
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException

object LspSessionConstructor {

    /**
     * Constructs the session with default error handling
     */
    fun constructDefault(state: LspSessionState): Boolean {
        return try {
            constructAsyncWithProgress(state).join()
            true
        } catch (ex: CompletionException) {
            val t = ex.cause

            // Only show any warning at all if the process wasn't just cancelled
            if (t !is ProcessCanceledException) {
                logger.warn("LSP session construction threw, destroying session", t)

                if (t is ResponseErrorException) {
                    LspIdea.showResponseError(
                        "Failed to initialize language server",
                        t.responseError,
                        state.project
                    )
                } else {
                    LspIdea.showError("Failed to initialize language server", state.project)
                }
            }

            LspSessionDestructor.destruct(state)

            false
        }
    }

    /**
     * Constructs the session asynchronously (the calling thread will not block)
     * under a background progress task.
     */
    fun constructAsyncWithProgress(
        state: LspSessionState,
        @Nls progressTitle: String = "Starting language server"
    ): CompletableFuture<Unit> {
        if (state.status != LspStatus.NOT_STARTED) {
            logger.error("Attempting to construct LSP session whose status is ${state.status}")
            return CompletableFuture.failedFuture(IllegalStateException("Session status is ${state.status}"))
        }

        return ProgressManager.getInstance().runBackgroundable(
            progressTitle,
            state.project,
            action = { indicator -> constructSync(state, indicator) }
        )
    }

    fun constructSync(state: LspSessionState, indicator: ProgressIndicator) {
        if (state.status != LspStatus.NOT_STARTED) {
            throw IllegalStateException("Attempting to construct LSP session whose status is ${state.status}")
        }

        state.status = LspStatus.STARTING

        indicator.text2 = "Spawning server process"
        startServer(state)

        indicator.checkCanceled()

        indicator.text2 = "Performing handshake"
        performHandshake(state, indicator)

        indicator.checkCanceled()

        logger.assertTrue(state.status == LspStatus.ACTIVE)
    }

    private fun startServer(state: LspSessionState) {
        state.status = LspStatus.STARTING

        val (inStream, outStream, infoStream) = state.process.start()

        val launcher = LSPLauncherEx.createClientLauncher(
            state.client,
            inStream,
            outStream,
            true,
            setDebugger = { state.debugger = it },
            setGson = { state.debugger.gson = it }
        )
        state.debugger.serverStderr = infoStream
        state.serverOrNull = launcher.remoteProxy
        launcher.startListening()

        state.status = LspStatus.UNINITIALIZED
    }

    private fun performHandshake(state: LspSessionState, indicator: ProgressIndicator) {
        state.status = LspStatus.INITIALIZING

        val initParams = createInitParams(state.project)
        initParams.initializationOptions = state.serverDef.initOptions
        if (state.process is LspLocalProcessRunner) {
            initParams.processId = Math.toIntExact(ProcessHandle.current().pid())
        }

        val response: InitializeResult = state.server.initialize(initParams)
            .joinUnwrapExceptionsCancellable(indicator = indicator)

        indicator.checkCanceled()

        state.serverInfoOrNull = response.serverInfo ?: ServerInfo("unnamed language server")
        state.serverCapabilitiesOrNull = response.capabilities

        processTextDocSyncSettings(response.capabilities.textDocumentSync ?: Either.forLeft(TextDocumentSyncKind.None))

        state.server.initialized(InitializedParams())
        state.status = LspStatus.ACTIVE
    }

    private fun processTextDocSyncSettings(sync: Either<TextDocumentSyncKind, TextDocumentSyncOptions>) {
        if (sync.isLeft) processTextDocSyncKind(sync.left)
        else processTextDocSyncOptions(sync.right)
    }

    private fun processTextDocSyncKind(syncKind: TextDocumentSyncKind) {
        when (syncKind) {
            TextDocumentSyncKind.None -> {
                // Nothing to do
            }
            TextDocumentSyncKind.Full -> TODO()
            TextDocumentSyncKind.Incremental -> {
            }
        }
    }

    private fun processTextDocSyncOptions(syncOptions: TextDocumentSyncOptions) {
        if (syncOptions.openClose == true) {
            // Need to send didOpen and didClose
            // TODO store this info somewhere
        }
        processTextDocSyncKind(syncOptions.change ?: TextDocumentSyncKind.None)
    }

    private fun createInitParams(project: Project?): InitializeParams {
        val initParams = InitializeParams()
        initParams.trace = TraceValue.Verbose
        initParams.capabilities = LspIdeaCapabilities.capabilities
        initParams.locale = Locale.getDefault().toLanguageTag()
        initParams.clientInfo = LspIdea.thePlugin.let { ClientInfo(it.name, it.version) }

        // TODO: Have a compatibility settings system of some kind
        val rootPath = project?.basePath?.let(Path::of)
        if (rootPath != null) {
            initParams.rootUri = rootPath.toUri().toURL().toString()
        }

        return initParams
    }

    private val logger = Logger.getInstance(javaClass)
}
