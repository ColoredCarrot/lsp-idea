package info.voidev.lspidea.symbol

import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.eclipse.lsp4j.DocumentSymbol

fun DocumentSymbolNavigable(
    project: Project,
    file: VirtualFile,
    symbol: DocumentSymbol,
) = OpenFileDescriptor(
    project,
    file,
    symbol.selectionRange.start.line,
    symbol.selectionRange.start.character
)
