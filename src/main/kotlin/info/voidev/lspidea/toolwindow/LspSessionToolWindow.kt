package info.voidev.lspidea.toolwindow

import com.google.gson.JsonObject
import com.intellij.codeInsight.hints.presentation.MouseButton
import com.intellij.codeInsight.hints.presentation.mouseButton
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.rd.createLifetime
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.Disposer
import com.intellij.ui.JBSplitter
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.components.JBPanelWithEmptyText
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.Label
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.tabs.JBTabsFactory
import com.jetbrains.rd.swing.mouseClicked
import info.voidev.lspidea.connect.LspSession
import info.voidev.lspidea.transientoptions.SessionDebugOptions
import info.voidev.lspidea.util.IoStreamBridge
import info.voidev.lspidea.util.addTab
import info.voidev.lspidea.util.bindSelectedDirectly
import info.voidev.lspidea.util.bindSelectedDirectlyToUserData
import info.voidev.lspidea.util.enabledWithSession
import org.apache.commons.io.output.WriterOutputStream
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.Timer

class LspSessionToolWindow(private val session: LspSession) : Disposable {

    private val project get() = session.project

    val mainComponent: JComponent

    private val disposed = AtomicBoolean(false)

    init {
        ApplicationManager.getApplication().assertIsDispatchThread()

        val tabs = JBTabsFactory.createTabs(project, this)
        tabs.addTab("Message Log", createMessageLog())
        tabs.addTab("stderr", createStderr())

        val split = JBSplitter(0.3f)
        split.firstComponent = createInfo()
        split.secondComponent = tabs.component

        mainComponent = split
    }

    override fun dispose() {
        if (!disposed.getAndSet(true)) {
            session.state.debugger.messageObserver = null
        }
    }

    private fun createInfo(): JComponent {
        val serverInfo = session.state.serverInfo

        @Suppress("UnstableApiUsage")
        return JBScrollPane(
            panel {
                collapsibleGroup("Information") {
                    row("Server:") {
                        cell(Label(serverInfo.name, bold = true))
                    }
                    row("Version:") {
                        val serverVersion = serverInfo.version ?: "unknown"
                        val serverVersionTextComp = label(serverVersion).component
                        serverVersionTextComp
                            .mouseClicked()
                            .advise(session.createLifetime()) { evt ->
                                if (evt.mouseButton == MouseButton.Left) {
                                    evt.consume()
                                    Toolkit.getDefaultToolkit()
                                        .systemClipboard
                                        .setContents(StringSelection(serverVersion), null)
                                    JBPopupFactory.getInstance()
                                        .createBalloonBuilder(JLabel("Copied"))
                                        .setFadeoutTime(2500)
                                        .createBalloon()
                                        .show(RelativePoint.getCenterOf(serverVersionTextComp), Balloon.Position.above)
                                }
                            }
                    }
                    row("Started:") {
                        val startedLocalTime = LocalTime.ofInstant(session.createdWhen, ZoneId.systemDefault())
                        label(startedLocalTime.format(TIME_FORMAT_HM))
                    }
                }.also { it.expanded = true }
                collapsibleGroup("Controls") {
                    row {
                        button("Stop") {
                            session.destroy()
                        }.enabledWithSession(session)
                    }
                }.also { it.expanded = true }
                collapsibleGroup("Debug Options") {
                    row("Capture:") {
                        checkBox("Contents").bindSelectedDirectly(session.state.debugger::captureContents)
                        checkBox("Stacktraces").bindSelectedDirectly(session.state.debugger::captureStackTraces)
                    }
                    row {
                        button("Fake Request") {
                            session.server.fakeRequest(JsonObject())
                        }.enabledWithSession(session)
                        button("Fake Notification") {
                            session.server.fakeNotification(JsonObject())
                        }.enabledWithSession(session)
                    }
                    row("Print raw documentation:") {
                        checkBox("Markdown").bindSelectedDirectlyToUserData(SessionDebugOptions.DOCS_RAW_MARKDOWN, session)
                        checkBox("HTML").bindSelectedDirectlyToUserData(SessionDebugOptions.DOCS_RAW_HTML, session)
                    }
                }
            }
        )
    }

    private fun createMessageLog(): JComponent {
        val messageLogUi = JrpcMessageLogView(project)
        Disposer.register(this, messageLogUi)

        session.state.debugger.messageObserver = messageLogUi
        return messageLogUi.component
    }

    private fun createStderr(): JComponent {
        val stderr = session.state.debugger.serverStderr
            ?: return JBPanelWithEmptyText()

        val comp = TextOutputComponent(project)
        Disposer.register(this, comp)

        // Bridge the server's stderr to the editor's input
        val bridge = IoStreamBridge(stderr, WriterOutputStream(comp.output, Charsets.UTF_8, 128, false))

        // On EDT: trigger the (non-blocking) transfer
        lateinit var timer: Timer
        timer = Timer(STDERR_UPDATE_PERIOD_MS) {
            if (disposed.get()) {
                timer.stop()
            } else {
                bridge.transferAvailable()
            }
        }
        timer.start()

        return comp.mainComponent
    }

    companion object {
        private val TIME_FORMAT_HM: DateTimeFormatter = DateTimeFormatterBuilder()
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .toFormatter()

        private const val STDERR_UPDATE_PERIOD_MS = 100
    }
}
