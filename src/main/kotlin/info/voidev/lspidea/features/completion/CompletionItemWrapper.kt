package info.voidev.lspidea.features.completion

import info.voidev.lspidea.connect.LspSession
import org.eclipse.lsp4j.CompletionItem

data class CompletionItemWrapper(val completionItem: CompletionItem, val session: LspSession)
