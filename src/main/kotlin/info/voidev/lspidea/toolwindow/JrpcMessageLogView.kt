package info.voidev.lspidea.toolwindow

import com.google.gson.Gson
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComponentContainer
import com.intellij.openapi.util.Disposer
import com.intellij.ui.CollectionListModel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ThreeState
import info.voidev.lspidea.lspex.debug.JrpcMessageCapture
import info.voidev.lspidea.lspex.debug.JrpcMessageDirection
import info.voidev.lspidea.lspex.debug.JrpcMessageKind
import info.voidev.lspidea.lspex.debug.JrpcMessageObserver
import info.voidev.lspidea.toolwindow.JrpcMessageLogView.Record.Companion.RESOLVE_ERROR
import info.voidev.lspidea.ui.OverlayLayout
import info.voidev.lspidea.util.installCellRendererWithBackgroundColors
import info.voidev.lspidea.util.installSpeedSearch
import info.voidev.lspidea.util.set
import info.voidev.lspidea.util.ui.JInvisible
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import java.awt.Component
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.ListCellRenderer
import javax.swing.SwingUtilities

/**
 * A Swing UI for displaying a log of JSON-RPC messages.
 */
class JrpcMessageLogView(private val project: Project, gson: Gson) : JrpcMessageObserver, ComponentContainer {

    private val mainComponent: JComponent

    private val messageDetailsViewFactory = JrpcMessageDetailsViewFactory(project, gson)

    private val myListCellRenderer = MyListCellRenderer()

    private val listWrapper: JPanel
    private val list = CollectionListModel<Record>()
    private val listComp: JBList<Record>

    /**
     * Maps record indices to details views.
     *
     * Views in this map are *not* registered as the child [Disposable]
     * of any disposable, including `this`;
     * instead, they are manually disposed (using [Disposer.dispose])
     * when they are collapsed or, at the latest, when `this` is disposed.
     */
    private val expandedDetailsViews = Int2ObjectOpenHashMap<JrpcMessageDetailsView>()

    private val sentRequests = Object2IntOpenHashMap<String>().also { it.defaultReturnValue(RESOLVE_ERROR) }
    private val receivedRequests = Object2IntOpenHashMap<String>().also { it.defaultReturnValue(RESOLVE_ERROR) }

    init {
        listWrapper = object : JPanel(OverlayLayout()) {
            // Return false here because the listWrapper's children may overlap
            override fun isOptimizedDrawingEnabled() = false
        }

        listComp = JBList(list)
        listComp.installCellRendererWithBackgroundColors(myListCellRenderer::getRenderComponent)
        listComp.installSpeedSearch { it.message.id.orEmpty() + " " + it.message.method.orEmpty() }

        listComp.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                e ?: return

                val clickedIndex = listComp.locationToIndex(e.point)
                if (clickedIndex !in 0 until list.size) return
                if (e.point !in listComp.getCellBounds(clickedIndex, clickedIndex)) return

                if (SwingUtilities.isLeftMouseButton(e) && e.clickCount == 2) {
                    triggerExpandOrCollapse(clickedIndex)
                    e.consume()
                }
            }
        })
        listComp.addKeyListener(object : KeyAdapter() {
            // FIXME not triggered
            override fun keyPressed(e: KeyEvent?) {
                e ?: return
                if (e.keyCode != KeyEvent.VK_LEFT && e.keyCode != KeyEvent.VK_RIGHT && e.keyCode != KeyEvent.VK_ENTER) {
                    return
                }

                for (selectedIndex in listComp.selectedIndices) {
                    triggerExpandOrCollapse(
                        selectedIndex,
                        when (e.keyCode) {
                            KeyEvent.VK_LEFT -> ThreeState.NO
                            KeyEvent.VK_RIGHT -> ThreeState.YES
                            KeyEvent.VK_ENTER -> ThreeState.UNSURE
                            else -> throw AssertionError()
                        }
                    )
                }
                e.consume()
            }
        })

        listWrapper.add(listComp, OverlayLayout.Base)

        mainComponent = JBScrollPane(listWrapper)
    }

    override fun getComponent() = mainComponent

    override fun getPreferredFocusableComponent() = listComp

    private fun triggerExpandOrCollapse(index: Int, shouldExpand: ThreeState = ThreeState.UNSURE) {
        val record = list.getElementAt(index)

        record.expanded = when (shouldExpand) {
            ThreeState.YES -> true
            ThreeState.NO -> false
            ThreeState.UNSURE -> !record.expanded
        }

        if (record.expanded) {
            val resolvedRecord = list.items.getOrNull(record.resolved)

            val recordLocation = listComp.indexToLocation(index)
            val expandedY = recordLocation.y + myListCellRenderer.unexpandedHeight
            val expandedComp = messageDetailsViewFactory(record.message, resolvedRecord?.message)

            listWrapper.add(expandedComp.component, OverlayLayout.Overlay(expandedY))
            expandedDetailsViews[index] = expandedComp
        } else {
            val expandedComp = expandedDetailsViews.remove(index)
            if (expandedComp != null) {
                listWrapper.remove(expandedComp.comp)
                Disposer.dispose(expandedComp)
            }
        }

        listComp.updateUI()
    }

    private inner class MyListCellRenderer : ListCellRenderer<Record> {

        // Note: ListCellRenderer uses "rubber stamp" rendering: reuse the same component instance for each cell

        private val recordView = JrpcMessageView(createDetailsView = {
            val messageDetailsView = messageDetailsViewFactory(it.message, null)
            // We don't need to dispose the details view because
            //  disposing is only required in case one of the buttons is clicked,
            //  which can never happen in this case (see below).

            // Wrap the details view in JInvisible:
            //  We just want the space to be allocated,
            //  but rendering will occur from actual live components
            //  that are overlaid on top of the JList.
            //  We do this because ListCellRenderer uses "rubber stamp" components that cannot
            //  actually react to button presses/etc.
            JInvisible(messageDetailsView.comp)
        })

        val unexpandedHeight get() = recordView.unexpandedHeight

        override fun getListCellRendererComponent(
            list: JList<out Record>,
            value: Record,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean,
        ): Component {
            return getRenderComponent(value)
        }

        fun getRenderComponent(record: Record): JComponent {
            return recordView.loadRecord(record)
        }
    }

    override fun observe(message: JrpcMessageCapture) {
        ApplicationManager.getApplication().invokeLater {

            val record = Record(message)
            val recordIndex = list.size

            if (message.kind == JrpcMessageKind.REQUEST) {
                val floatingRequests =
                    if (message.direction == JrpcMessageDirection.SENT) sentRequests
                    else receivedRequests

                floatingRequests[message.id] = recordIndex
            } else if (message.kind == JrpcMessageKind.RESPONSE) {
                val floatingRequests =
                    if (message.direction == JrpcMessageDirection.SENT) receivedRequests
                    else sentRequests

                record.resolved = floatingRequests.removeInt(message.id)
                if (record.resolved >= 0) {
                    list.getElementAt(record.resolved)!!.resolved = recordIndex
                }
            }

            list.add(record)
        }
    }

    private val disposed = AtomicBoolean(false)

    override fun dispose() {
        if (!disposed.getAndSet(true)) {
            for (messageDetailsView in expandedDetailsViews.values) {
                Disposer.dispose(messageDetailsView)
            }
            expandedDetailsViews.clear()
        }
    }

    class Record(val message: JrpcMessageCapture) {
        var resolved =
            if (message.kind == JrpcMessageKind.REQUEST || message.kind == JrpcMessageKind.RESPONSE) NOT_YET_RESOLVED
            else NOT_APPLICABLE

        var expanded = false

        companion object {
            const val NOT_APPLICABLE = -3
            const val NOT_YET_RESOLVED = -2
            const val RESOLVE_ERROR = -1
        }
    }
}
