package info.voidev.lspidea.features.rename

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileTypes.FileTypes
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.refactoring.ui.NameSuggestionsField
import com.intellij.refactoring.ui.RefactoringDialog
import com.intellij.util.ui.JBUI
import com.intellij.xml.util.XmlStringUtil
import info.voidev.lspidea.connect.LspSession
import info.voidev.lspidea.connect.LspStatus
import info.voidev.lspidea.editor.applyWorkspaceEdit
import info.voidev.lspidea.util.offset2lspPosition
import org.eclipse.lsp4j.RenameParams
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.WorkspaceEdit
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class LspRenameDialog(
    private val session: LspSession,
    private val fileIdentifier: TextDocumentIdentifier,
    project: Project,
    private val editor: Editor,
) : RefactoringDialog(project, true) {

    private lateinit var myNameLabel: JLabel
    private lateinit var myNameSuggestionsField: NameSuggestionsField

    init {
        myNameSuggestionsField =
            object : NameSuggestionsField(arrayOf(), myProject, FileTypes.PLAIN_TEXT, editor) {
                override fun shouldSelectAll(): Boolean {
                    return editor == null || editor.settings.isPreselectRename
                }
            }

        init()

        myNameLabel.text = XmlStringUtil.wrapInHtml(XmlStringUtil.escapeString("Rename symbol", false))

        if (!ApplicationManager.getApplication().isUnitTestMode) validateButtons()
    }

    override fun hasHelpAction(): Boolean {
        return false
    }

    override fun createCenterPanel(): JComponent? {
        return null
    }

    override fun createNorthPanel(): JComponent? {
        val panel = JPanel(GridBagLayout())
        val gbConstraints = GridBagConstraints()

        gbConstraints.insets = JBUI.insetsBottom(4)
        gbConstraints.weighty = 0.0
        gbConstraints.weightx = 1.0
        gbConstraints.gridwidth = GridBagConstraints.REMAINDER
        gbConstraints.fill = GridBagConstraints.BOTH
        myNameLabel = JLabel()
        panel.add(myNameLabel, gbConstraints)

        gbConstraints.insets = JBUI.insetsBottom(8)
        gbConstraints.gridwidth = 2
        gbConstraints.fill = GridBagConstraints.BOTH
        gbConstraints.weightx = 1.0
        gbConstraints.gridx = 0
        gbConstraints.weighty = 1.0
        panel.add(myNameSuggestionsField.component, gbConstraints)

        return panel
    }

    private fun getNewName(): String {
        return myNameSuggestionsField.enteredName.trim()
    }

    override fun doAction() {
        if (editor.isDisposed || !session.isActive) return
        val newName = getNewName().takeIf { it.isNotEmpty() } ?: return

        isOKActionEnabled = false

        session.server.textDocumentService.rename(RenameParams(
            fileIdentifier,
            editor.document.offset2lspPosition(editor.caretModel.currentCaret.offset),
            newName
        )).handle { workspaceEdit: WorkspaceEdit?, ex: Throwable? ->
            if (ex != null) {
                if (ex is ResponseErrorException) {
//                    setErrorText(ex.responseError.message, myNameSuggestionsField)
                    updateErrorInfo(listOf(ValidationInfo(ex.responseError.message, myNameSuggestionsField)))
                } else {
                    logger.error("Failed to perform rename via LSP", ex)
                }
                isOKActionEnabled = true
            } else if (workspaceEdit != null) {
                applyWorkspaceEdit(session, workspaceEdit, "Rename symbol to $newName")
                closeOKAction()
            }
        }
    }

    companion object {
        private val logger = logger<LspRenameDialog>()
    }
}
