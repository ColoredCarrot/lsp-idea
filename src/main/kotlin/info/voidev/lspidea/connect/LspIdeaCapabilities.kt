package info.voidev.lspidea.connect

import info.voidev.lspidea.features.highlight.LspHighlightingMap
import org.eclipse.lsp4j.ClientCapabilities
import org.eclipse.lsp4j.CodeActionCapabilities
import org.eclipse.lsp4j.CodeActionKind
import org.eclipse.lsp4j.CodeActionKindCapabilities
import org.eclipse.lsp4j.CodeActionLiteralSupportCapabilities
import org.eclipse.lsp4j.CodeActionResolveSupportCapabilities
import org.eclipse.lsp4j.CodeLensCapabilities
import org.eclipse.lsp4j.CodeLensWorkspaceCapabilities
import org.eclipse.lsp4j.CompletionCapabilities
import org.eclipse.lsp4j.CompletionItemCapabilities
import org.eclipse.lsp4j.CompletionItemResolveSupportCapabilities
import org.eclipse.lsp4j.CompletionItemTag
import org.eclipse.lsp4j.CompletionItemTagSupportCapabilities
import org.eclipse.lsp4j.DeclarationCapabilities
import org.eclipse.lsp4j.DefinitionCapabilities
import org.eclipse.lsp4j.DiagnosticTag
import org.eclipse.lsp4j.DiagnosticsTagSupport
import org.eclipse.lsp4j.DocumentHighlightCapabilities
import org.eclipse.lsp4j.DocumentSymbolCapabilities
import org.eclipse.lsp4j.ExecuteCommandCapabilities
import org.eclipse.lsp4j.FoldingRangeCapabilities
import org.eclipse.lsp4j.FoldingRangeKind
import org.eclipse.lsp4j.FoldingRangeKindSupportCapabilities
import org.eclipse.lsp4j.FoldingRangeSupportCapabilities
import org.eclipse.lsp4j.GeneralClientCapabilities
import org.eclipse.lsp4j.HoverCapabilities
import org.eclipse.lsp4j.MarkdownCapabilities
import org.eclipse.lsp4j.MarkupKind
import org.eclipse.lsp4j.OnTypeFormattingCapabilities
import org.eclipse.lsp4j.ParameterInformationCapabilities
import org.eclipse.lsp4j.PublishDiagnosticsCapabilities
import org.eclipse.lsp4j.ReferencesCapabilities
import org.eclipse.lsp4j.ResourceOperationKind
import org.eclipse.lsp4j.SelectionRangeCapabilities
import org.eclipse.lsp4j.SemanticTokensCapabilities
import org.eclipse.lsp4j.SemanticTokensClientCapabilitiesRequests
import org.eclipse.lsp4j.SemanticTokensClientCapabilitiesRequestsFull
import org.eclipse.lsp4j.ShowDocumentCapabilities
import org.eclipse.lsp4j.SignatureHelpCapabilities
import org.eclipse.lsp4j.SignatureInformationCapabilities
import org.eclipse.lsp4j.SymbolKind
import org.eclipse.lsp4j.SymbolKindCapabilities
import org.eclipse.lsp4j.SymbolTag
import org.eclipse.lsp4j.SymbolTagSupportCapabilities
import org.eclipse.lsp4j.SynchronizationCapabilities
import org.eclipse.lsp4j.TextDocumentClientCapabilities
import org.eclipse.lsp4j.TokenFormat
import org.eclipse.lsp4j.WindowClientCapabilities
import org.eclipse.lsp4j.WindowShowMessageRequestActionItemCapabilities
import org.eclipse.lsp4j.WindowShowMessageRequestCapabilities
import org.eclipse.lsp4j.WorkspaceClientCapabilities
import org.eclipse.lsp4j.WorkspaceEditCapabilities

object LspIdeaCapabilities {

    val capabilities by lazy(LazyThreadSafetyMode.PUBLICATION) { createCapabilities() }


    fun createCapabilities() = ClientCapabilities().apply {
        general = createGeneral()
        textDocument = createTextDocument()
        window = createWindow()
        workspace = createWorkspace()
    }


    private fun createGeneral() = GeneralClientCapabilities().apply {
        markdown = createMarkdown()
    }

    private fun createMarkdown() = MarkdownCapabilities().apply {
        parser = "IntelliJ Markdown"
        version = "unknown"
    }


    private fun createTextDocument() = TextDocumentClientCapabilities().apply {
        synchronization = createSynchronization()
        completion = createCompletion()
        semanticTokens = createSemanticTokens()
        publishDiagnostics = createDiagnostics()
        codeAction = createCodeAction()
        signatureHelp = createSignatureHelp()
        codeLens = createCodeLens()
        foldingRange = createFoldingRange()
        documentSymbol = createDocumentSymbol()
        hover = createHover()
        declaration = createDeclaration()
        definition = createDefinition()
        references = ReferencesCapabilities()
        selectionRange = SelectionRangeCapabilities()
        onTypeFormatting = OnTypeFormattingCapabilities()
        documentHighlight = DocumentHighlightCapabilities()
    }

    private fun createSynchronization() = SynchronizationCapabilities().apply {
        willSave = false//TODO true
        willSaveWaitUntil = false//TODO true
        didSave = true
    }

    private fun createCompletion() = CompletionCapabilities().apply {
        completionItem = createCompletionItem()
        contextSupport = false//TODO true
    }

    private fun createCompletionItem() = CompletionItemCapabilities().apply {
        tagSupport = CompletionItemTagSupportCapabilities(listOf(
            CompletionItemTag.Deprecated,
        ))
        deprecatedSupport = true
        documentationFormat = listOf(MarkupKind.MARKDOWN, MarkupKind.PLAINTEXT)
        commitCharactersSupport = false//TODO
        insertReplaceSupport = true
        snippetSupport = true
        // We can resolve all those attributes which are only needed at insertion
        resolveSupport = CompletionItemResolveSupportCapabilities(listOf("textEdit", "additionalTextEdits", "command"))
    }

    private fun createSemanticTokens() = SemanticTokensCapabilities(
        /* requests = */ createSemanticTokensRequest(),
        /* tokenTypes = */ LspHighlightingMap.SUPPORTED_TOKENS,
        /* tokenModifiers = */ LspHighlightingMap.SUPPORTED_MODS,
        /* formats = */ listOf(TokenFormat.Relative)
    ).apply {
        multilineTokenSupport = true
        overlappingTokenSupport = false
    }

    private fun createSemanticTokensRequest() = SemanticTokensClientCapabilitiesRequests().apply {
        setRange(false)//TODO
        setFull(SemanticTokensClientCapabilitiesRequestsFull(true))
    }

    private fun createDiagnostics() = PublishDiagnosticsCapabilities().apply {
        relatedInformation = true
        setTagSupport(DiagnosticsTagSupport(listOf(DiagnosticTag.Deprecated, DiagnosticTag.Unnecessary)))
        codeDescriptionSupport = false//TODO
        dataSupport = false//TODO

        //TODO: this seems a bit broken; e.g. rust-analyzer doesn't send new diagnostics for each version, so how to interpret?
        versionSupport = false
    }

    private fun createCodeAction() = CodeActionCapabilities().apply {
        codeActionLiteralSupport = CodeActionLiteralSupportCapabilities().apply {
            codeActionKind = CodeActionKindCapabilities(listOf(
                CodeActionKind.QuickFix,
                CodeActionKind.SourceOrganizeImports,
                CodeActionKind.Refactor, CodeActionKind.RefactorInline, CodeActionKind.RefactorExtract
            ))
        }
        isPreferredSupport = false//TODO
        disabledSupport = true//TODO make sure this is handled as early as possible (not just in the executor)
        resolveSupport = CodeActionResolveSupportCapabilities(listOf())//TODO
    }

    private fun createSignatureHelp() = SignatureHelpCapabilities().apply {
        signatureInformation = SignatureInformationCapabilities().apply {
            documentationFormat = listOf(MarkupKind.MARKDOWN, MarkupKind.PLAINTEXT)
            parameterInformation = ParameterInformationCapabilities().apply {
                labelOffsetSupport = true
            }
            activeParameterSupport = true
        }
        contextSupport = true
    }

    private fun createCodeLens() = CodeLensCapabilities().apply {
    }

    private fun createFoldingRange() = FoldingRangeCapabilities().apply {
        lineFoldingOnly = false
        foldingRangeKind = FoldingRangeKindSupportCapabilities(listOf(
            FoldingRangeKind.Comment, FoldingRangeKind.Imports, FoldingRangeKind.Region
        ))
        foldingRange = FoldingRangeSupportCapabilities().apply {
            collapsedText = true
        }
    }

    private fun createDocumentSymbol() = DocumentSymbolCapabilities().apply {
        symbolKind = SymbolKindCapabilities(SymbolKind.values().asList())
        hierarchicalDocumentSymbolSupport = true
        tagSupport = SymbolTagSupportCapabilities(listOf(SymbolTag.Deprecated))
        labelSupport = true
    }

    private fun createHover() = HoverCapabilities().apply {
        contentFormat = listOf(MarkupKind.MARKDOWN, MarkupKind.PLAINTEXT)
    }

    private fun createDeclaration() = DeclarationCapabilities().apply {
        linkSupport = true
    }

    private fun createDefinition() = DefinitionCapabilities().apply {
        linkSupport = true
    }


    private fun createWindow() = WindowClientCapabilities().apply {
        showMessage = createShowMessage()
        showDocument = ShowDocumentCapabilities(true)
        workDoneProgress = true
    }

    private fun createShowMessage() = WindowShowMessageRequestCapabilities().apply {
        messageActionItem = WindowShowMessageRequestActionItemCapabilities(false)
    }


    private fun createWorkspace() = WorkspaceClientCapabilities().apply {
        applyEdit = true
        workspaceEdit = createWorkspaceEdit()

        codeLens = CodeLensWorkspaceCapabilities(true)
        executeCommand = ExecuteCommandCapabilities()
    }

    private fun createWorkspaceEdit() = WorkspaceEditCapabilities().apply {
        documentChanges = false // TODO
        resourceOperations = listOf(
            ResourceOperationKind.Create,
            ResourceOperationKind.Delete,
            ResourceOperationKind.Rename,
        )
        normalizesLineEndings = true
        //TODO failureHandling
    }

}
