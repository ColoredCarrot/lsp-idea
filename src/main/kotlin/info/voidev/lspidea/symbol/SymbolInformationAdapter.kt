package info.voidev.lspidea.symbol

import org.eclipse.lsp4j.SymbolInformation
import org.eclipse.lsp4j.SymbolTag
import org.eclipse.lsp4j.WorkspaceSymbol
import org.eclipse.lsp4j.jsonrpc.messages.Either

fun SymbolInformation.adapt(): WorkspaceSymbol {
    val adapter = WorkspaceSymbol(
        name,
        kind,
        Either.forLeft(location),
        containerName
    )

    var tags = tags.orEmpty()
    if (deprecated == true && SymbolTag.Deprecated !in tags) {
        tags = tags + SymbolTag.Deprecated
    }
    adapter.tags = tags

    return adapter
}
