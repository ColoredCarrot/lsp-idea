package info.voidev.lspidea.editor

import com.intellij.openapi.command.UndoConfirmationPolicy
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import info.voidev.lspidea.LspIdea
import info.voidev.lspidea.connect.LspSession
import info.voidev.lspidea.util.LspUtils
import org.eclipse.lsp4j.ApplyWorkspaceEditResponse
import org.eclipse.lsp4j.ResourceOperation
import org.eclipse.lsp4j.TextDocumentEdit
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier
import org.eclipse.lsp4j.WorkspaceEdit
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode

fun applyWorkspaceEdit(session: LspSession, edit: WorkspaceEdit, label: String?): ApplyWorkspaceEditResponse {
    return try {
        WriteCommandAction
            .writeCommandAction(session.project)
            .let { if (label != null) it.withName(label) else it }
            .withUndoConfirmationPolicy(UndoConfirmationPolicy.REQUEST_CONFIRMATION)
            .run<Throwable> {
                doApplyWorkspaceEdit(edit)
            }

        ApplyWorkspaceEditResponse(true)
    } catch (e: Exception) {
        logger<LspIdea>().error(e)

        ApplyWorkspaceEditResponse(false)
    }
}

private fun doApplyWorkspaceEdit(edit: WorkspaceEdit) {
    val changes = edit.documentChanges
        ?: edit.changes.map { (uri, editsInFile) ->
            Either.forLeft(
                TextDocumentEdit(
                    VersionedTextDocumentIdentifier(uri, -1/*TODO better this*/),
                    editsInFile
                )
            )
        }
        ?: emptyList()
    for (change in changes) {
        if (change.isLeft) doApplyEdit(change.left)
        else if (change.isRight) doApplyEdit(change.right)
    }
}

private fun doApplyEdit(edit: TextDocumentEdit) {
    val file = LspUtils.resolve(edit.textDocument.uri)
        ?: throw ResponseErrorException(
            ResponseError(
                ResponseErrorCode.InvalidRequest,
                "Trying to edit a file that doesn't exist",
                edit
            )
        )

    // TODO: Validate that the edited file is reasonable, to protect ourselves

    val document = FileDocumentManager.getInstance().getDocument(file)
        ?: throw ResponseErrorException(
            ResponseError(
                ResponseErrorCode.InternalError,
                "Failed to open file",
                edit
            )
        )

    document.applyEdits(edit.edits)
}

private fun doApplyEdit(edit: ResourceOperation) {
    TODO()
}
