package info.voidev.lspidea.features.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionInitializationContext
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import info.voidev.lspidea.LspSessionManager
import info.voidev.lspidea.util.offset2lspPosition

class LspCompletionContributor : CompletionContributor() {

    override fun beforeCompletion(context: CompletionInitializationContext) {
        context.replacementOffset = context.startOffset
    }

    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        val project = parameters.editor.project ?: return
        val file = parameters.originalFile.virtualFile ?: return
        val session = LspSessionManager.getInstance(project).getForFile(file) ?: return

        val pos = parameters.editor.document.offset2lspPosition(parameters.offset)

        LspCompletionProvider(parameters.editor, session).getCompletions(pos, result)
    }
}
