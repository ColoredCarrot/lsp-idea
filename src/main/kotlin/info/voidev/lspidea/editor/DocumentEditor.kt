package info.voidev.lspidea.editor

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import info.voidev.lspidea.util.lspPosition2offset
import info.voidev.lspidea.util.reverseConsecutiveSequences
import org.eclipse.lsp4j.TextEdit

fun Document.applyEdits(edits: Collection<TextEdit>) {
    val sortedEdits = resolveEdits(edits)
    sortedEdits.sortForApplyingToDocument()

    for (edit in sortedEdits) {
        edit.applyTo(this)
    }
}

fun Document.resolveEdits(edits: Collection<TextEdit>) =
    edits.mapTo(ArrayList(edits.size)) { ResolvedTextEdit(it, this) }

/**
 * Sorts the text edits in this list
 * such that they are ready to be applied consecutively to a document.
 */
fun MutableList<out ResolvedTextEdit>.sortForApplyingToDocument() {
    // Sort edits because they're all relative to the initial state; they don't build on each other.
    // The sort is stable.
    sortDescending()

    // We have to reverse all consecutive sequences of same-offset edits,
    //  since the LSP specification demands that the order in which they occur in the array
    //  be the order in which the strings *appear* in the output
    //  (so not the one they need to be inserted in).
    reverseConsecutiveSequences()
}

open class ResolvedTextEdit(val newText: String, val start: Int, val end: Int) : Comparable<ResolvedTextEdit> {
    constructor(fromEdit: TextEdit, document: Document) : this(
        fromEdit.newText,
        document.lspPosition2offset(fromEdit.range.start),
        document.lspPosition2offset(fromEdit.range.end)
    )

    override fun compareTo(other: ResolvedTextEdit) = start.compareTo(other.start)

    open fun applyTo(document: Document, moveCaretInEditor: Editor? = null) {
        val normalizedText = newText.replace("\r\n", "\n").replace('\r', '\n')

        if (start == end) {
            document.insertString(start, normalizedText)
        } else if (newText.isEmpty()) {
            document.deleteString(start, end)
        } else {
            document.replaceString(start, end, normalizedText)
        }

        if (moveCaretInEditor != null) {
            val caret = moveCaretInEditor.caretModel.currentCaret
            caret.removeSelection()
            caret.moveToOffset(start + newText.length)
        }
    }
}
