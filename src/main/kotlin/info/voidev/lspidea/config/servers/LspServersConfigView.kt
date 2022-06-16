package info.voidev.lspidea.config.servers

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.ui.ComponentContainer
import com.intellij.openapi.ui.Splitter
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.Disposer
import com.intellij.ui.CollectionListModel
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.LayeredIcon
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBPanelWithEmptyText
import info.voidev.lspidea.def.LspServerSupport
import java.awt.BorderLayout
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.JPanel

class LspServersConfigView : ComponentContainer {

    /*
    Notes:
    We maintain a list model of Entry instances,
    each of which models a language server instance
    (e.g. there may be multiple instances of a rust-analyzer server).

    For each Entry,
    its config view is always a live component;
    the only question is whether it is visible.
    Only the selected Entry's view is visible.

    The views are disposed when they are removed from the list
    or, alternatively, when this view is disposed.
     */

    private val serverListModel = CollectionListModel<LspServerConfig>()
    private val serverList = JBList(serverListModel)

    private val mainComponent: JComponent

    /**
     * The panel that contains (only) the server config views.
     */
    private val serverConfigContainer: JPanel

    private val isDisposed = AtomicBoolean(false)

    init {
        serverConfigContainer = JBPanelWithEmptyText(BorderLayout())
            .withEmptyText("No language server selected.")

        val split = Splitter(false, 0.3f)
        split.firstComponent = createServerList()
        split.secondComponent = serverConfigContainer

        mainComponent = split
    }

    var serverConfigs: List<LspServerConfig>
        get() {
            logger.assertTrue(!isDisposed.get())
            ApplicationManager.getApplication().assertIsDispatchThread()

            return serverListModel.items
        }
        set(value) {
            logger.assertTrue(!isDisposed.get())
            ApplicationManager.getApplication().assertIsDispatchThread()

            val remainingNewConfiguredServers = ArrayList(value)

            // We want to reuse instances that already exist in the UI
            for (oldConfiguredServer in ArrayList(serverListModel.items)) {
                val newConfiguredServer = value.firstOrNull { it.id == oldConfiguredServer.id }

                if (newConfiguredServer == null) {
                    serverConfigContainer.remove(oldConfiguredServer.component)
                    Disposer.dispose(oldConfiguredServer)
                    serverListModel.remove(oldConfiguredServer)
                    continue
                }

                remainingNewConfiguredServers -= newConfiguredServer

                oldConfiguredServer.reset(newConfiguredServer.apply())
            }

            for (newConfiguredServer in remainingNewConfiguredServers) {
                serverListModel.add(newConfiguredServer)
                serverConfigContainer.add(newConfiguredServer.component)
            }

            showServerConfig()
        }

    private fun showServerConfig(idx: Int = serverList.selectedIndex) {
        ApplicationManager.getApplication().assertIsDispatchThread()

        serverConfigContainer.components.forEach { it.isVisible = false }

        if (idx >= 0 && idx < serverListModel.size) {
            serverListModel.getElementAt(idx).component.isVisible = true
        }
    }

    private fun removeFromList(idx: Int) {
        ApplicationManager.getApplication().assertIsDispatchThread()

        if (idx < 0 || idx >= serverListModel.size) {
            return
        }

        val removedEntry = serverListModel.getElementAt(idx)
        serverConfigContainer.remove(removedEntry.component.also { it.isVisible = false })
        serverListModel.remove(idx)
        Disposer.dispose(removedEntry)

        showServerConfig()
    }

    private fun createServerList(): JPanel {
        serverList.cellRenderer = ServerListCellRenderer()

        // Update the right-hand side whenever the user selects a different language server
        serverList.addListSelectionListener { e ->
            if (!e.valueIsAdjusting) showServerConfig()
        }

        val serverListToolbar = ToolbarDecorator.createDecorator(serverList)
        attachAddServerActionTo(serverListToolbar)
        serverListToolbar.setRemoveAction {
            removeFromList(serverList.selectedIndex)
        }

        return serverListToolbar.createPanel()
    }

    private fun attachAddServerActionTo(toolbar: ToolbarDecorator) {
        // Collect the available language server configuration providers
        val createActions = LspServerSupport.EP_NAME.extensionList
            .map(this::AddServerAction)

        if (createActions.isEmpty()) {
            // This should never happen, as there should always at least be the "Generic" one
            toolbar.disableAddAction()
            return
        }

        // The "Add" button opens a dropdown to allow user to select language server type (i.e. a server config provider)
        // See TaskRepositoriesConfigurable for another usage of this dropdown UI
        toolbar.setAddIcon(LayeredIcon.ADD_WITH_DROPDOWN)
        toolbar.setAddAction { addActionButton ->
            val addActionGroup = DefaultActionGroup(createActions)
            addActionGroup.isPopup = true
            // Note: Separators in the popup can be created with
            //  group.add(com.intellij.openapi.actionSystem.Separator.create("Whatever"))

            JBPopupFactory.getInstance()
                .createActionGroupPopup(
                    "Add Language Server",
                    addActionGroup,
                    addActionButton.dataContext,
                    JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                    true
                )
                .show(addActionButton.preferredPopupPoint)
        }
    }

    private inner class AddServerAction(
        private val provider: LspServerSupport<*>,
    ) : AnAction(provider.server.displayName, null, provider.server.icon), DumbAware {
        override fun actionPerformed(e: AnActionEvent) {
            logger.assertTrue(!isDisposed.get())

            val newEntry = LspServerConfig(provider)
            newEntry.reset(newEntry.createDefaults())

            serverListModel.add(newEntry)
            newEntry.component.isVisible = false
            serverConfigContainer.add(newEntry.component)
        }
    }

    private class ServerListCellRenderer : ColoredListCellRenderer<LspServerConfig>() {
        override fun customizeCellRenderer(
            list: JList<out LspServerConfig>,
            value: LspServerConfig,
            index: Int,
            selected: Boolean,
            hasFocus: Boolean,
        ) {
            icon = value.origin.server.icon
            append(value.liveGivenName)
        }
    }

    override fun getComponent() = mainComponent

    override fun getPreferredFocusableComponent(): JComponent? = null

    override fun dispose() {
        if (isDisposed.getAndSet(true)) {
            // Was already disposed
            return
        }

        for (item in serverListModel.items) {
            serverConfigContainer.remove(item.component)
            Disposer.dispose(item)
        }
    }

    companion object {
        private val logger = logger<LspServersConfigView>()
    }
}
