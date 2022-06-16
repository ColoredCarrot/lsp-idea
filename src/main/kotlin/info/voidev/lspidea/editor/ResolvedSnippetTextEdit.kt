package info.voidev.lspidea.editor

import com.intellij.codeInsight.template.TemplateManager
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import info.voidev.lspidea.snippet.LspSnippet
import info.voidev.lspidea.snippet.LspSnippetTemplateInstantiator
import org.eclipse.lsp4j.InsertTextFormat
import org.eclipse.lsp4j.TextEdit

open class ResolvedSnippetTextEdit(
    fromEdit: TextEdit,
    private val insertTextFormat: InsertTextFormat?,
    private val project: Project,
    private val editor: Editor,
    private val definitelyMoveCaret: Boolean = false,
) : ResolvedTextEdit(fromEdit, editor.document) {

    override fun applyTo(document: Document, moveCaretInEditor: Editor?) {
        if (moveCaretInEditor != null) {
            thisLogger().assertTrue(moveCaretInEditor === editor)
        }

        val delegateMoveCaretInEditor = if (definitelyMoveCaret) editor else moveCaretInEditor

        if ((insertTextFormat ?: InsertTextFormat.PlainText) == InsertTextFormat.Snippet) {
            val snippet = LspSnippet(newText)
            if (snippet.isSimple) {
                return super.applyTo(document, delegateMoveCaretInEditor)
            }

            // Delete the snippet's replacement region
            if (end > start) {
                document.deleteString(start, end)
            }

            // Move the caret to the correct position to start the snippet
            editor.caretModel.moveToOffset(start)

            // Actually run the snippet
            val template = LspSnippetTemplateInstantiator.instantiate(snippet)
            TemplateManager.getInstance(project).runTemplate(editor, template)
        } else {
            super.applyTo(document, delegateMoveCaretInEditor)
        }
    }
}
