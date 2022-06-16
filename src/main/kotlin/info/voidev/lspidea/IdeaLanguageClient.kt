package info.voidev.lspidea

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.jetbrains.rd.util.string.printToString
import info.voidev.lspidea.connect.LspSession
import info.voidev.lspidea.connect.LspStatus
import info.voidev.lspidea.diagnostics.LspDiagnosticsManager
import info.voidev.lspidea.editor.applyWorkspaceEdit
import info.voidev.lspidea.misc.LspAction
import info.voidev.lspidea.misc.LspNotification
import info.voidev.lspidea.util.LspUtils
import org.eclipse.lsp4j.ApplyWorkspaceEditParams
import org.eclipse.lsp4j.ApplyWorkspaceEditResponse
import org.eclipse.lsp4j.LogTraceParams
import org.eclipse.lsp4j.MessageActionItem
import org.eclipse.lsp4j.MessageParams
import org.eclipse.lsp4j.MessageType
import org.eclipse.lsp4j.ProgressParams
import org.eclipse.lsp4j.PublishDiagnosticsParams
import org.eclipse.lsp4j.ShowDocumentParams
import org.eclipse.lsp4j.ShowDocumentResult
import org.eclipse.lsp4j.ShowMessageRequestParams
import org.eclipse.lsp4j.WorkDoneProgressBegin
import org.eclipse.lsp4j.WorkDoneProgressCreateParams
import org.eclipse.lsp4j.WorkDoneProgressEnd
import org.eclipse.lsp4j.WorkDoneProgressKind
import org.eclipse.lsp4j.WorkDoneProgressReport
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode
import org.eclipse.lsp4j.services.LanguageClient
import java.util.concurrent.CompletableFuture

class IdeaLanguageClient(private val project: Project?) : LanguageClient {

    //FIXME: Sometimes throws because access-before-init in e.g. reportProgress
    internal var sessionOrNull: LspSession? = null
    private val session get() = sessionOrNull!!

    private val logger = Logger.getInstance(javaClass)

    override fun telemetryEvent(`object`: Any?) {
        logger.debug("Received telemetry event from language server: " + `object`.printToString())
    }

    override fun publishDiagnostics(diagnostics: PublishDiagnosticsParams) {
        if (project != null) {
            LspDiagnosticsManager.getInstance(project).publishDiagnostics(diagnostics)
        }
    }

    override fun showMessage(messageParams: MessageParams) {
        LspNotification.group()
            .createNotification(messageParams.message, messageParams.type.asNotificationType())
            .notify(project)
    }

    override fun showMessageRequest(requestParams: ShowMessageRequestParams): CompletableFuture<MessageActionItem> {
        val future = CompletableFuture<MessageActionItem>()

        LspNotification.requestGroup()
            .createNotification(requestParams.message, requestParams.type.asNotificationType())
            .addActions(requestParams.actions.map { actionItem ->
                LspAction(actionItem, future)
            } as Collection<AnAction>)
            .notify(project)

        return future
    }

    override fun logTrace(params: LogTraceParams) {
        val msg = if (params.verbose != null) params.message + "  |  " + params.verbose else params.message
        logger.warn(msg) // TODO change to trace
    }

    override fun logMessage(message: MessageParams) {
        when (message.type ?: MessageType.Info) {
            MessageType.Error -> logger.error(message.message)
            MessageType.Warning -> logger.warn(message.message)
            MessageType.Info, MessageType.Log -> logger.info(message.message)
        }
    }

    override fun showDocument(params: ShowDocumentParams): CompletableFuture<ShowDocumentResult> {
        if (project == null) {
            return CompletableFuture.failedFuture(IllegalStateException("project is null"))
        }

        val future = CompletableFuture<ShowDocumentResult>()
        ApplicationManager.getApplication().invokeLater {
            try {
                val file = LspUtils.resolve(params.uri)
                if (file == null) {
                    future.complete(ShowDocumentResult(false))
                } else {
                    OpenFileDescriptor(project, file).navigate(params.takeFocus ?: true)
                    future.complete(ShowDocumentResult(true))
                }
            } catch (ex: Exception) {
                future.completeExceptionally(ex)
            }
        }
        return future
    }

    override fun createProgress(params: WorkDoneProgressCreateParams): CompletableFuture<Void> {
        // All our state is created with notifyProgress, so we needn't do anything here
        return CompletableFuture.completedFuture(null)
    }

    override fun notifyProgress(params: ProgressParams) {
        if (sessionOrNull == null) return

        if (session.state.status !in listOf(LspStatus.INITIALIZING, LspStatus.ACTIVE, LspStatus.FINALIZING)) {
            throw ResponseErrorException(ResponseError(
                ResponseErrorCode.InvalidRequest,
                "Cannot notifyProgress for a session whose status is ${session.state.status}",
                params
            ))
        }

        session.streamingSupport.notifyProgress(params)

        val dto = params.value.left
        when (dto?.kind) {
            WorkDoneProgressKind.begin -> session.progressManager.begin(params.token, dto as WorkDoneProgressBegin)
            WorkDoneProgressKind.report -> session.progressManager.report(params.token, dto as WorkDoneProgressReport)
            WorkDoneProgressKind.end -> session.progressManager.end(params.token, dto as WorkDoneProgressEnd)
            null -> {}
        }
    }

    override fun refreshCodeLenses(): CompletableFuture<Void> {
        // We don't refresh code lenses manually,
        // as IntelliJ will do it very often anyway and
        // many language servers send this event every time a document is edited
        return CompletableFuture.completedFuture(null)
    }

    override fun refreshSemanticTokens(): CompletableFuture<Void> {
        DaemonCodeAnalyzer.getInstance(project).restart()
        return CompletableFuture.completedFuture(null)
    }

    override fun applyEdit(params: ApplyWorkspaceEditParams): CompletableFuture<ApplyWorkspaceEditResponse> {
        return CompletableFuture.completedFuture(
            applyWorkspaceEdit(session, params.edit, params.label ?: "Edit requested by ${session.state.serverInfo.name}")
        )
    }
}
