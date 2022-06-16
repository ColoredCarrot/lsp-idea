package info.voidev.lspidea.features.codeaction

import org.eclipse.lsp4j.ServerCapabilities

val ServerCapabilities.mayFetchCodeActions
    get() = codeActionProvider?.let { it.left ?: it.isRight } == true
