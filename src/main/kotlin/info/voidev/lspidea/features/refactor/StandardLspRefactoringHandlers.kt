package info.voidev.lspidea.features.refactor

import org.eclipse.lsp4j.CodeActionKind

object LspIntroduceVariableHandler : SimpleCodeActionBasedLspRefactoringHandler(
    refactoringName = "Introduce Variable",
    kindFilter = CodeActionKind.RefactorExtract,
    titleFilter = listOf("variable"),
)

object LspIntroduceConstantHandler : SimpleCodeActionBasedLspRefactoringHandler(
    refactoringName = "Introduce Constant",
    kindFilter = CodeActionKind.RefactorExtract,
    titleFilter = listOf("constant"),
)

object LspExtractModuleHandler : SimpleCodeActionBasedLspRefactoringHandler(
    refactoringName = "Extract Module",
    kindFilter = CodeActionKind.RefactorExtract,
    titleFilter = listOf("module"),
)

object LspExtractMethodHandler : SimpleCodeActionBasedLspRefactoringHandler(
    refactoringName = "Extract Method",
    kindFilter = CodeActionKind.RefactorExtract,
    titleFilter = listOf("method", "function"),
)

object LspInlineHandler : SimpleCodeActionBasedLspRefactoringHandler(
    refactoringName = "Inline Symbol",
    kindFilter = CodeActionKind.RefactorInline,
)
