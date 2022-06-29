package info.voidev.lspidea.util

import com.intellij.icons.AllIcons
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.notification.NotificationType
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.SelectionModel
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.util.DocumentUtil
import info.voidev.lspidea.LspIdea
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.objects.Object2IntMap
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.DiagnosticSeverity
import org.eclipse.lsp4j.InsertReplaceEdit
import org.eclipse.lsp4j.Location
import org.eclipse.lsp4j.MessageType
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.ServerCapabilities
import org.eclipse.lsp4j.SymbolKind
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.TextEdit
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode
import org.eclipse.lsp4j.util.Ranges
import org.jetbrains.annotations.Nls
import java.net.URI
import java.net.URISyntaxException
import java.nio.file.FileSystemNotFoundException
import java.nio.file.Path
import java.util.concurrent.CancellationException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.swing.Icon

// TODO: This file is a dump... refactor it

object LspUtils {
    fun identify(document: Document) = identify(FileDocumentManager.getInstance().getFile(document)!!)

    fun identify(file: VirtualFile): String {
        return parseVfsUrl(file.url).toASCIIString()
    }

    fun resolve(uri: String): VirtualFile? {
        // The easy case:
        VirtualFileManager.getInstance().findFileByUrl(uri)
            ?.also { return it }

        val parsedUri = try {
            URI(uri)
        } catch (_: URISyntaxException) {
            return null
        }

        // Maybe we just had to normalize the URI?
        VirtualFileManager.getInstance().findFileByUrl(parsedUri.toString())
            ?.also { return it }

        val parsedUriAsPath = try {
            Path.of(parsedUri)
        } catch (_: IllegalArgumentException) {
            return null
        } catch (_: FileSystemNotFoundException) {
            return null
        }

        return VirtualFileManager.getInstance().findFileByNioPath(parsedUriAsPath)
    }

    private fun parseVfsUrl(url: String): URI {
        return URI(url)
    }
}

fun VirtualFile.identifyForLsp() = TextDocumentIdentifier(LspUtils.identify(this))
fun Document.identifyForLsp() = TextDocumentIdentifier(LspUtils.identify(this))
fun TextDocumentIdentifier.resolve() = LspUtils.resolve(uri)
fun Location.resolveFile() = LspUtils.resolve(uri)

fun Document.offset2lspPosition(offset: Int): Position {
    val line = getLineNumber(offset)
    val lineStart = getLineStartOffset(line)
    return Position(line, offset - lineStart)
}

fun Document.lspPosition2offset(pos: Position): Int {
    return lspPosition2offset(pos.line, pos.character)
}

fun Document.lspPosition2offset(line: Int, character: Int): Int {
    return DocumentUtil.calculateOffset(this, line, character, 1)
//    if (line == lineCount && line >= 1 && character <= 0) {
//        return getLineEndOffset(line - 1) + character
//    }
//    return getLineStartOffset(line) + character
}

fun Document.range2lspRange(range: TextRange) = range2lspRange(range.startOffset, range.endOffset)

fun Document.range2lspRange(start: Int, end: Int) = Range(
    offset2lspPosition(start),
    offset2lspPosition(end)
)

fun Document.lspRange2range(range: Range) = TextRange(
    lspPosition2offset(range.start),
    lspPosition2offset(range.end)
)

/**
 * Same as [Document.getLineEndOffset] but with invalid lines wrapped to be always valid.
 */
fun Document.getLineEndOffsetWrap(line: Int): Int {
    if (line < 0) return 0
    if (line >= lineCount) return textLength
    return getLineEndOffset(line)
}

val PositionComparator: Comparator<Position> = Comparator
    .comparingInt(Position::getLine)
    .thenComparingInt(Position::getCharacter)

operator fun Position.compareTo(that: Position) = PositionComparator.compare(this, that)

operator fun Range.contains(inner: Range) =
    Ranges.containsRange(this, inner)

val Editor.caretLspPosition get() = document.offset2lspPosition(caretModel.offset)

val CompletionItemKind?.icon
    get(): Icon? = when (this) {
        CompletionItemKind.Text -> null
        CompletionItemKind.Method -> AllIcons.Nodes.Method
        CompletionItemKind.Function -> AllIcons.Nodes.Function
        CompletionItemKind.Constructor -> AllIcons.Nodes.ClassInitializer
        CompletionItemKind.Field -> AllIcons.Nodes.Field
        CompletionItemKind.Variable -> AllIcons.Nodes.Variable
        CompletionItemKind.Class -> AllIcons.Nodes.Class
        CompletionItemKind.Interface -> AllIcons.Nodes.Interface
        CompletionItemKind.Module -> AllIcons.Nodes.Module
        CompletionItemKind.Property -> AllIcons.Nodes.Property
        CompletionItemKind.Unit -> null
        CompletionItemKind.Value -> null
        CompletionItemKind.Enum -> AllIcons.Nodes.Enum
        CompletionItemKind.Keyword -> null
        CompletionItemKind.Snippet -> null
        CompletionItemKind.Color -> AllIcons.Actions.Colors
        CompletionItemKind.File -> AllIcons.FileTypes.Any_type
        CompletionItemKind.Reference -> null
        CompletionItemKind.Folder -> AllIcons.Nodes.Folder
        CompletionItemKind.EnumMember -> AllIcons.Nodes.Constant
        CompletionItemKind.Constant -> AllIcons.Nodes.Constant
        CompletionItemKind.Struct -> AllIcons.Nodes.Class
        CompletionItemKind.Event -> AllIcons.Ide.Notification.InfoEvents
        CompletionItemKind.Operator -> CompletionItemKind.Method.icon
        CompletionItemKind.TypeParameter -> AllIcons.Nodes.Type
        null -> null
    }

val SymbolKind?.icon
    get(): Icon? = when (this) {
        SymbolKind.File -> AllIcons.FileTypes.Any_type
        SymbolKind.Module -> AllIcons.Modules.SourceRoot
        SymbolKind.Namespace -> AllIcons.Nodes.Package
        SymbolKind.Package -> AllIcons.Nodes.Package
        SymbolKind.Class -> AllIcons.Nodes.Class
        SymbolKind.Struct -> AllIcons.Nodes.Class
        SymbolKind.Method -> AllIcons.Nodes.Method
        SymbolKind.Property -> AllIcons.Nodes.Property
        SymbolKind.Field -> AllIcons.Nodes.Field
        SymbolKind.Constructor -> AllIcons.Nodes.ClassInitializer
        SymbolKind.Enum -> AllIcons.Nodes.Enum
        SymbolKind.Interface -> AllIcons.Nodes.Interface
        SymbolKind.Variable -> AllIcons.Nodes.Variable
        SymbolKind.Constant -> AllIcons.Nodes.Constant
        SymbolKind.EnumMember -> AllIcons.Nodes.Constant
        SymbolKind.TypeParameter -> AllIcons.Nodes.Parameter
        SymbolKind.Operator,
        SymbolKind.Function,
        -> AllIcons.Nodes.Function
        SymbolKind.Array,
        SymbolKind.Object,
        SymbolKind.Key,
        SymbolKind.Null,
        SymbolKind.Event,
        SymbolKind.Boolean,
        SymbolKind.Number,
        SymbolKind.String,
        -> AllIcons.Nodes.Tag
        null -> null
    }

fun <R> CompletableFuture<R>.joinLsp(project: Project, responseErrorTitle: String? = null): R? {
    @Suppress("UNCHECKED_CAST")
    return (this as CompletableFuture<R?>).joinUnwrapExceptionsCancellable(ifResponseError = {
        LspIdea.showResponseError(responseErrorTitle ?: "Language Server Error", it.responseError, project)
        null
    })
}

fun <R> Future<R>.joinUnwrapExceptionsCancellable(
    propagateResponseError: Boolean = true,
    ifResponseError: (ex: ResponseErrorException) -> R = {
        throw if (propagateResponseError) it else ProcessCanceledException(it)
    },
    indicator: ProgressIndicator? = null,
): R {
    var elapsedTime = 0L
    do {
        try {
            return get(CANCELLED_REFRESH_INTERVAL_MILLIS, TimeUnit.MILLISECONDS) as R
        } catch (ex: CancellationException) {
            throw ProcessCanceledException(ex)
        } catch (ex: InterruptedException) {
            throw ProcessCanceledException(ex)
        } catch (ex: ExecutionException) {
            val cause = ex.cause
            if (cause is ResponseErrorException) {
                if (cause.responseError?.code == ResponseErrorCode.RequestCancelled.value) {
                    throw ProcessCanceledException(cause)
                }
                return ifResponseError(cause)
            }
            throw cause ?: ex
        } catch (_: TimeoutException) {
            // ignore
        }

        try {
            if (indicator != null) indicator.checkCanceled()
            else ProgressManager.checkCanceled()
        } catch (ex: ProcessCanceledException) {
            // Propagate cancellation to future
            cancel(true)
            throw ex
        }

        elapsedTime += CANCELLED_REFRESH_INTERVAL_MILLIS
    } while (elapsedTime < ABSOLUTE_TIMEOUT_MILLIS)

    throw TimeoutException("LSP request timed out after $elapsedTime ms")
}

private const val CANCELLED_REFRESH_INTERVAL_MILLIS = 100L

private const val ABSOLUTE_TIMEOUT_MILLIS = 60 * 1000L // 1 min

inline fun <T> ProgressManager.runBackgroundable(
    @Nls(capitalization = Nls.Capitalization.Sentence) title: String,
    project: Project?,
    cancelable: Boolean = true,
    crossinline action: (indicator: ProgressIndicator) -> T,
): CompletableFuture<T> {
    val future = CompletableFuture<T>()
    run(object : Task.Backgroundable(project, title, cancelable) {
        override fun run(indicator: ProgressIndicator) {
            try {
                future.complete(action(indicator))
            } catch (ex: Throwable) {
                future.completeExceptionally(ex)
            }
        }
    })
    return future
}

inline operator fun <T, R> ((T) -> R).plus(crossinline next: (T) -> R): (T) -> R = { this(it); next(it) }

fun ServerCapabilities.supportsRangeFormatting() =
    documentRangeFormattingProvider?.let { it.isRight || it.left } == true

fun DiagnosticSeverity?.asHighlightSeverity(): HighlightSeverity = when (this) {
    DiagnosticSeverity.Error -> HighlightSeverity.ERROR
    DiagnosticSeverity.Warning -> HighlightSeverity.WARNING
    DiagnosticSeverity.Hint -> HighlightSeverity.WEAK_WARNING
    DiagnosticSeverity.Information, null -> HighlightSeverity.INFORMATION
}

fun Document.append(s: CharSequence) {
    insertString(textLength, s)
}

// operator fun JTextComponent.getValue(thisRef: Any?, property: KProperty<*>): String {
//    return text
// }
//
// operator fun JTextComponent.setValue(thisRef: Any?, property: KProperty<*>, value: String) {
//    text = value
// }

inline fun <T, reified R> List<T>.mapToArray(f: (T) -> R) = Array(size) { i -> f(this[i]) }

fun InsertReplaceEdit.getEdit(isReplace: Boolean) = TextEdit(if (isReplace) replace else insert, newText)

fun Either<TextEdit, InsertReplaceEdit>.getEdit(isReplace: Boolean) = left ?: right.getEdit(isReplace)

val SelectionModel.selectionRange get() = TextRange(selectionStart, selectionEnd)

@Suppress("NOTHING_TO_INLINE")
inline operator fun <K> Object2IntMap<K>.set(key: K, value: Int) = put(key, value)

@Suppress("NOTHING_TO_INLINE")
inline operator fun <V> Int2ObjectMap<V>.set(key: Int, value: V?): V? = put(key, value)

fun MessageType.asNotificationType() = when (this) {
    MessageType.Error -> NotificationType.ERROR
    MessageType.Warning -> NotificationType.WARNING
    MessageType.Info, MessageType.Log -> NotificationType.INFORMATION
}
