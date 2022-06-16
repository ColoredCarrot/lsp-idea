package info.voidev.lspidea.features.joinlines

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler

@Deprecated("This is just a backup plan if it turns out that we need the precise caret positions")
class UNUSEDLspJoinLinesActionHandler : EditorActionHandler() {

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext?) {
        super.doExecute(editor, caret, dataContext)
    }
}
