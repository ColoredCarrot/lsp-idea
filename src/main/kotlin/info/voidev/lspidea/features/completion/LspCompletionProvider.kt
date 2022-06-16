package info.voidev.lspidea.features.completion

import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.AutoCompletionPolicy
import com.intellij.codeInsight.lookup.Lookup
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.collaboration.async.CompletableFutureUtil.handleOnEdt
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Editor
import com.intellij.util.Consumer
import info.voidev.lspidea.command.LspCommandExecutionUtil
import info.voidev.lspidea.connect.LspSession
import info.voidev.lspidea.editor.ResolvedSnippetTextEdit
import info.voidev.lspidea.editor.ResolvedTextEdit
import info.voidev.lspidea.editor.sortForApplyingToDocument
import info.voidev.lspidea.errors.LspErrors
import info.voidev.lspidea.util.getEdit
import info.voidev.lspidea.util.icon
import info.voidev.lspidea.util.identifyForLsp
import info.voidev.lspidea.util.joinLsp
import info.voidev.lspidea.util.joinUnwrapExceptionsCancellable
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.CompletionItemTag
import org.eclipse.lsp4j.CompletionParams
import org.eclipse.lsp4j.Position

/**
 * Very short-lived object
 */
class LspCompletionProvider(private val editor: Editor, private val session: LspSession) {

    private val identifier = editor.document.identifyForLsp()

    fun getCompletions(pos: Position, out: Consumer<LookupElement>) {
        if (!serverSupportsCompletion()) {
            return
        }

        session.server.textDocumentService
            .completion(CompletionParams(identifier, pos))
            .handleOnEdt(session) { response, throwable ->
                if (throwable != null || response == null) {
                    LspErrors.mildError("Could not fetch completions", throwable)
                    return@handleOnEdt
                }

                val completionItems = if (response.isLeft) response.left else response.right.items
                val isIncomplete = response.isRight && response.right.isIncomplete //TODO

                //TODO test sorting
                completionItems.asSequence()
                    .sortedByDescending { item -> item.sortText?.takeIf { it.isNotBlank() } ?: item.label!! }
                    .mapNotNull(::createLookupElement)
                    .mapIndexed { index, lookupElement ->
                        // We've sorted the completion items in reverse order, so lower index should come last
                        // => this matches withPriority, where a higher priority corresponds to appearing earlier
                        PrioritizedLookupElement.withPriority(lookupElement, index.toDouble())
                    }
                    .forEach(out::consume)
            }
            // Unfortunately, it seems that CompletionContributors are not expected to be async
            .joinLsp(session.project)
    }

    private fun serverSupportsCompletion(): Boolean {
        return session.state.serverCapabilities.completionProvider != null
    }

    private fun serverSupportsCompletionResolve(): Boolean {
        return session.state.serverCapabilities.completionProvider?.resolveProvider == true
    }

    private fun createLookupElement(item: CompletionItem): LookupElement? {
        val tags = item.tags.orEmpty()
        val text = item.textEdit?.let { it.left?.newText ?: it.right.newText } ?: item.insertText ?: item.label!!

        return LookupElementBuilder
            .create(CompletionItemWrapper(item, session), text)
            .withIcon(item.kind.icon)
            .withBoldness(item.kind == CompletionItemKind.Keyword)
            .withPresentableText(item.label.takeIf { it.isNotBlank() } ?: item.insertText ?: return null)
            .withStrikeoutness(CompletionItemTag.Deprecated in tags)
            .withTypeText(item.detail, true)
            .withInsertHandler { context, elem -> handleInsert(context, elem) }
            .withAutoCompletionPolicy(AutoCompletionPolicy.SETTINGS_DEPENDENT)
    }

    private fun handleInsert(context: InsertionContext, elem: LookupElement) {
        var item = (elem.`object` as CompletionItemWrapper).completionItem

        // Resolve the item if possible
        if (session.isActive && serverSupportsCompletionResolve()) {
            item = session.server.textDocumentService.resolveCompletionItem(item)
                .joinUnwrapExceptionsCancellable()
                ?: item
        }

        val document = context.document

        if (item.textEdit == null || (!item.textEdit.isLeft && !item.textEdit.isRight)) {
            // "Dumb mode": Let IntelliJ handle the completion (which it already has)
            if (!item.additionalTextEdits.isNullOrEmpty()) {
                thisLogger().info(
                    "CompletionItem.additionalTextEdits is set and non-empty even though textEdit is not set. " +
                            "This is not yet supported. Dropping additional edits."
                )
            }
            return
        }

        val isReplace = context.completionChar == Lookup.REPLACE_SELECT_CHAR

        // If replacing instead of inserting:
        // IntelliJ has replaced a word already such that
        // [context.startOffset, context.tailOffset) is now mainEdit.newText
        // Effectively, as an extra step when inserting,
        // IntelliJ has before anything deleted [caret, tailOffset)

        // Clear IntelliJ's insertion
//        if (!isReplace) {
            document.deleteString(context.startOffset, context.tailOffset)
//        }

        val mainEdit = item.textEdit.getEdit(isReplace)
        val moreEdits = item.additionalTextEdits.orEmpty()

        val allEdits = ArrayList<ResolvedTextEdit>(1 + (item.additionalTextEdits?.size ?: 0))

        // Add the main edit
        allEdits += ResolvedSnippetTextEdit(mainEdit, item.insertTextFormat, context.project, context.editor, true)

        // Add additional edits
        allEdits += moreEdits.map { ResolvedTextEdit(it, document) }

        allEdits.sortForApplyingToDocument()
        allEdits.forEach { it.applyTo(document) }

        context.commitDocument()

        // Finally, there might be a command to execute
        item.command?.also { LspCommandExecutionUtil.execute(it, session) }
    }

}
