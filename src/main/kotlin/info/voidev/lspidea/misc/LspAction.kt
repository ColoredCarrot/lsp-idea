package info.voidev.lspidea.misc

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import org.eclipse.lsp4j.MessageActionItem
import java.util.concurrent.CompletableFuture

class LspAction(
    private val actionItem: MessageActionItem,
    private val callback: CompletableFuture<MessageActionItem>,
) : AnAction(actionItem.title) {
    override fun actionPerformed(e: AnActionEvent) {
        callback.complete(actionItem)
    }
}
