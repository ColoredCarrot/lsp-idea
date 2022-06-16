package info.voidev.lspidea.features.refactor

import com.intellij.openapi.util.NlsContexts
import org.eclipse.lsp4j.CodeAction

open class SimpleCodeActionBasedLspRefactoringHandler(
    @NlsContexts.DialogTitle refactoringName: String,
    private val kindFilter: String? = null,
    private val titleFilter: Collection<String>? = null,
) : AbstractCodeActionBasedLspRefactoringHandler(refactoringName) {

    override fun canUseCodeAction(action: CodeAction): Boolean {
        if (kindFilter != null && action.kind != kindFilter) {
            return false
        }

        if (!titleFilter.isNullOrEmpty()) {
            val title = action.title.lowercase()
            if (titleFilter.none { it in title }) {
                return false
            }
        }

        return true
    }
}
