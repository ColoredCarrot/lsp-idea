package info.voidev.lspidea.features.codeaction

import com.intellij.codeHighlighting.TextEditorHighlightingPass
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerEx
import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.codeInsight.daemon.impl.HighlightInfoType
import com.intellij.codeInsight.daemon.impl.UpdateHighlightersUtil
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import info.voidev.lspidea.LspIdea
import info.voidev.lspidea.LspSessionManager
import info.voidev.lspidea.diagnostics.LspCodeActionIntentionAction
import info.voidev.lspidea.misc.toCodeAction
import info.voidev.lspidea.util.identifyForLsp
import info.voidev.lspidea.util.joinUnwrapExceptionsCancellable
import info.voidev.lspidea.util.range2lspRange
import org.eclipse.lsp4j.CodeAction
import org.eclipse.lsp4j.CodeActionContext
import org.eclipse.lsp4j.CodeActionParams
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException

class LspCodeActionsHighlightingPass(project: Project, private val editor: Editor) :
    TextEditorHighlightingPass(project, editor.document, false) {

    private var cachedSelectionRange: TextRange? = null
    private var highlights: List<HighlightInfo> = emptyList()

    /** Document modification timestamp of when code actions where fetched from the LS */
    private var fetchedWhen = 0L

    override fun doCollectInformation(progress: ProgressIndicator) {
        try {
            return doCollectInformation0(progress)
        } catch (ex: ResponseErrorException) {
            //TODO figure out a cool way to handle this
            LspIdea.showResponseError("Could not fetch code actions", ex.responseError, myProject)
        }
    }

    private fun doCollectInformation0(progress: ProgressIndicator) {
        if (editor.isDisposed) return

        // This will be called a lot,
        // including whenever the caret is moved (throttled).

        // TODO: Move all the code that fetches available actions to LspCodeActionManager

        val file = FileDocumentManager.getInstance().getFile(document) ?: return
        val session = LspSessionManager.getInstance(myProject).getForFile(file) ?: return

        if (!session.state.serverCapabilities.mayFetchCodeActions) return

        val range = TextRange(editor.selectionModel.selectionStart, editor.selectionModel.selectionEnd)
        val rangeForLsp = document.range2lspRange(range)

        val actions: List<CodeAction> = session.server.textDocumentService.codeAction(CodeActionParams(
            document.identifyForLsp(),
            rangeForLsp,
            CodeActionContext(emptyList())
        )).joinUnwrapExceptionsCancellable()
            .orEmpty()
            .map { it.right ?: it.left.toCodeAction() }

        session.codeActionManager.setAvailableActions(document, range, actions)

        highlights = actions.map { action ->
            val highlight = HighlightInfo
                .newHighlightInfo(HighlightInfoType.INFORMATION)
                .range(range)
                .createUnconditionally()
            highlight.registerFix(LspCodeActionIntentionAction(action), null, null, range, null)
            highlight
        }

        cachedSelectionRange = range
        fetchedWhen = document.modificationStamp
    }

    override fun doApplyInformationToEditor() {
        if (editor.isDisposed) return
        if (editor.document.modificationStamp != fetchedWhen) return

        // See com.intellij.codeInsight.daemon.impl.ExternalToolPass:259
        UpdateHighlightersUtil.setHighlightersToEditor(
            myProject,
            editor.document,
            0, editor.document.textLength,
            highlights,
            colorsScheme,
            id
        )
        DaemonCodeAnalyzerEx.getInstanceEx(myProject).fileStatusMap.markFileUpToDate(myDocument, id)
    }

    override fun getInfos(): List<HighlightInfo> {
        if (editor.isDisposed) return emptyList()

        // The range that we fetched the infos for
        val range = cachedSelectionRange ?: return emptyList()

        if (editor.selectionModel.selectionStart != range.startOffset ||
            editor.selectionModel.selectionEnd != range.endOffset
        ) {
            return emptyList()
        }

        return highlights
    }
}
