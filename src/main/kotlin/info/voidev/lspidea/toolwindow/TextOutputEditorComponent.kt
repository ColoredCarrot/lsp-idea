package info.voidev.lspidea.toolwindow

import com.intellij.execution.impl.ConsoleViewUtil
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ShutDownTracker
import com.intellij.util.text.CharArrayCharSequence
import info.voidev.lspidea.util.append
import java.io.Writer

class TextOutputEditorComponent(project: Project) : Writer(), Disposable {

    val editor: EditorEx

    init {
        editor = ConsoleViewUtil.setupConsoleEditor(project, false, false)
        editor.settings.isWhitespacesShown = false
    }

    override fun dispose() {
        EditorFactory.getInstance().releaseEditor(editor)
    }

    override fun write(cbuf: CharArray, off: Int, len: Int) {
        if (editor.isDisposed || ShutDownTracker.isShutdownHookRunning()) return
        ApplicationManager.getApplication().assertIsDispatchThread()

        val document: Document = editor.document
        val scroll = document.textLength == editor.caretModel.offset || !editor.contentComponent.hasFocus()

        document.append(CharArrayCharSequence(cbuf, off, off + len))

        if (scroll) {
            editor.caretModel.moveToOffset(document.textLength)
            editor.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)
        }
    }

    override fun close() {
    }

    override fun flush() {
    }
}
