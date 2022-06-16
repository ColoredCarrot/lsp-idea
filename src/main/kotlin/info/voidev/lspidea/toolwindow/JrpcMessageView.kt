package info.voidev.lspidea.toolwindow

import com.intellij.icons.AllIcons
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import info.voidev.lspidea.lspex.debug.JrpcMessageDirection
import org.eclipse.lsp4j.jsonrpc.messages.ResponseMessage
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Font
import java.awt.font.TextAttribute
import javax.swing.JPanel
import javax.swing.SwingConstants

/**
 * A UI component for JSON-RPC messages.
 *
 * Usable as a "rubber stamp" component for rapid rendering as part of a list.
 */
open class JrpcMessageView(
    private val createDetailsView: (JrpcMessageLogView.Record) -> Component,
) : JPanel(BorderLayout()) {

    private val methodLabel = JBLabel().withBorder(JBUI.Borders.empty(4))

    val unexpandedHeight get() = methodLabel.height

    /**
     * Sets up this component to show the given [record].
     */
    open fun loadRecord(record: JrpcMessageLogView.Record): JrpcMessageView {
        val m = record.message

        // Reset rubber stamp components
        removeAll()

        methodLabel.text = m.method.orEmpty() + m.id?.let { " ($it)" }.orEmpty()
        methodLabel.horizontalAlignment = when (m.direction) {
            JrpcMessageDirection.SENT -> SwingConstants.LEFT
            JrpcMessageDirection.RECEIVED -> SwingConstants.RIGHT
            null -> SwingConstants.CENTER
        }

        // Set cell color based on the message's status
        val bg = when {
            (m.message as? ResponseMessage)?.error != null -> JBColor.RED
            record.resolved == JrpcMessageLogView.Record.RESOLVE_ERROR -> JBColor.RED
            record.resolved == JrpcMessageLogView.Record.NOT_YET_RESOLVED -> JBColor.YELLOW
            else -> null
        }
        if (bg != null) {
            background = bg
            methodLabel.font = methodLabel.font?.deriveFont(Font.BOLD)
                ?: Font(mapOf(TextAttribute.WEIGHT to TextAttribute.WEIGHT_BOLD))
        } else {
            background = null
            methodLabel.font = null
        }

        add(
            methodLabel,
            when (m.direction) {
                JrpcMessageDirection.SENT -> BorderLayout.WEST
                JrpcMessageDirection.RECEIVED -> BorderLayout.EAST
                null -> BorderLayout.CENTER
            }
        )

        // Only make message expandable if there is something to show
        if (m.message != null || m.stackTrace != null) {
            if (!record.expanded) {
                methodLabel.icon = AllIcons.Actions.ArrowExpand
            } else {
                methodLabel.icon = AllIcons.Actions.ArrowCollapse
                add(createDetailsView(record), BorderLayout.PAGE_END)
            }
        }

        return this
    }

}
