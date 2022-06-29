package info.voidev.lspidea.toolwindow

import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.json.JsonFileType
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.Row
import com.intellij.ui.dsl.builder.panel
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProviderBase
import com.intellij.xdebugger.impl.frame.XStandaloneVariablesView
import info.voidev.lspidea.debug.DebugNodeFactory
import info.voidev.lspidea.debug.DebugNodeXStackFrame
import info.voidev.lspidea.lspex.debug.JrpcMessageCapture
import info.voidev.lspidea.lspex.debug.JrpcMessageKind
import info.voidev.lspidea.lspex.debug.StackTrace
import info.voidev.lspidea.ui.printStackTrace
import org.eclipse.lsp4j.jsonrpc.messages.NotificationMessage
import org.eclipse.lsp4j.jsonrpc.messages.RequestMessage
import org.eclipse.lsp4j.jsonrpc.messages.ResponseMessage
import java.lang.reflect.Type
import javax.swing.JComponent

class JrpcMessageDetailsViewFactory(private val project: Project) {

    /**
     * Creates a Swing component that acts as a details view for a given message [m].
     *
     * @param m The message for which the details view is requested.
     * @param resolved The resolved message associated with [m]
     *                 (either its request if [m] is a response or
     *                 its response if [m] is a request),
     *                 or `null` if resolution failed or is not yet complete.
     */
    operator fun invoke(m: JrpcMessageCapture, resolved: JrpcMessageCapture?): JrpcMessageDetailsView {
        return Builder(m, resolved).build()
    }

    @Suppress("UnstableApiUsage")
    private inner class Builder(private val m: JrpcMessageCapture, private val resolved: JrpcMessageCapture?) {

        private lateinit var parentDisposable: Disposable

        private val theRequest = when (m.kind) {
            JrpcMessageKind.REQUEST -> m
            JrpcMessageKind.RESPONSE -> resolved
            else -> null
        }

        private val theResponse = when (m.kind) {
            JrpcMessageKind.REQUEST -> resolved
            JrpcMessageKind.RESPONSE -> m
            else -> null
        }

        private val theNotification =
            if (m.kind == JrpcMessageKind.NOTIFICATION) m
            else null

        fun build(): JrpcMessageDetailsView {
            val result = JrpcMessageDetailsView()
            parentDisposable = result

            result.comp = panel {
                row {
                    descriptiveSentence()
                }

                if (m.message != null || m.stackTrace != null) {
                    row {
                        label("View:")

                        if (theNotification?.message != null) {
                            link("Contents") { showData((theNotification.message as NotificationMessage).params) }
                        } else {
                            viewContentsLinksForRequestResponse()
                        }

                        // If we are a response,
                        // the request's stacktrace is much more relevant.
                        if (m.kind == JrpcMessageKind.RESPONSE && resolved?.stackTrace != null) {
                            link("Stacktrace") { showStackTrace(resolved.stackTrace) }
                        } else if (m.stackTrace != null) {
                            link("Stacktrace") { showStackTrace(m.stackTrace) }
                        }
                    }
                }
            }

            return result
        }

        private fun Row.descriptiveSentence() {
            // Construct a descriptive sentence, like
            // "Request for textDocument/codeLens was answered with an error"
            // "Response for textDocument/codeLens contains an error"

            val sentence = StringBuilder()
            sentence
                .append(m.kind?.displayName ?: "Unknown message")
                .append(" for ")
                .append(m.method ?: resolved?.method ?: "unknown method")

            if (m.kind == JrpcMessageKind.REQUEST) {
                if (resolved == null) {
                    sentence.append(" has not yet been answered")
                } else {
                    sentence.append(" was answered")
                    when {
                        (resolved.message as ResponseMessage?)?.error != null -> sentence.append(" with an error")
                        resolved.message != null -> sentence.append(" successfully")
                    }
                }
            } else if (m.kind == JrpcMessageKind.RESPONSE) {
                if ((m.message as ResponseMessage?)?.error != null) {
                    sentence.append(" contains an error")
                } else if (m.message != null) {
                    sentence.append(" was successful")
                }
            }

            sentence.append('.')

            label(sentence.toString())
        }

        private fun Row.viewContentsLinksForRequestResponse() {
            if (theRequest?.message != null) {
                link("Request") { showData((theRequest.message as RequestMessage).params) }
            }
            if (theResponse?.message != null) {
                link("Response") { showResponse(theResponse.message as ResponseMessage) }
            }
        }

        /**
         * Shows some message data (either a request's parameters or a response's data)
         * in a new modal dialog.
         */
        private fun showData(data: Any, type: Type? = null) {
            object : DialogWrapper(project) {
                init {
                    title = "View Contents"
                    init()
                }

                override fun createCenterPanel(): JComponent {
                    // We use IntelliJ's standard debugging UI to render the tree
                    val variablesView = XStandaloneVariablesView(
                        project,
                        object : XDebuggerEditorsProviderBase() {
                            override fun getFileType() = JsonFileType.INSTANCE

                            override fun createExpressionCodeFragment(
                                project: Project,
                                text: String,
                                context: PsiElement?,
                                isPhysical: Boolean,
                            ): PsiFile {
                                throw UnsupportedOperationException()
                            }
                        },
                        DebugNodeXStackFrame(DebugNodeFactory.create(data, type))
                    )

                    Disposer.register(parentDisposable, variablesView)

                    val tree = variablesView.tree
//                    TreeUtil.expand(tree, )
                    tree.expandNodesOnLoad { node ->
//                        // TODO Only expand those nodes that don't have a custom renderer
                        false
                    }

                    return JBScrollPane(tree)
                }
            }.show()
        }

        private fun showResponse(message: ResponseMessage) {
            if (message.error != null) {
                showData(message.error)
            } else if (message.result != null) {
                showData(message.result)
            }
        }

        /**
         * Opens a new modal dialog in which [stackTrace]
         * is shown in a read-only console.
         */
        private fun showStackTrace(stackTrace: StackTrace) {
            object : DialogWrapper(project) {
                init {
                    title = "View Stacktrace"
                    init()
                }

                override fun createCenterPanel(): JComponent {
                    val console = TextConsoleBuilderFactory.getInstance().createBuilder(project).console
                    Disposer.register(parentDisposable, console)

                    // TODO: print hyperlinks in case we detect a development instance (maybe?)
                    console.printStackTrace(stackTrace)

                    return console.component
                }
            }.show()
        }
    }
}
