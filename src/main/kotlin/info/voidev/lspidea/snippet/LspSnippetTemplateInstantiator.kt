package info.voidev.lspidea.snippet

import com.intellij.codeInsight.template.impl.TemplateImpl
import com.intellij.codeInsight.template.impl.TextExpression
import com.intellij.codeInsight.template.impl.Variable
import info.voidev.lspidea.util.sortedBy

object LspSnippetTemplateInstantiator {

    fun instantiate(snippet: LspSnippet): TemplateImpl {
        val template = TemplateImpl("", "")

        createTabStopVariables(snippet, template)

        for (component in snippet.components) {
            when (component) {
                is LspSnippetComponent.Text -> template.addTextSegment(component.string)
                is LspSnippetComponent.TabStop -> {
                    if (component.index == 0u) {
                        // The end caret position
                        template.addEndVariable()
                    } else {
                        template.addVariableSegment(component.variableName)
                    }
                }
            }
        }

        return template
    }

    private fun createTabStopVariables(snippet: LspSnippet, template: TemplateImpl) {
        // Luckily, as the snippet has already been validated,
        // we can assume everything to be valid at this stage.
        snippet.components.asSequence()
            .filterIsInstance<LspSnippetComponent.TabStop>()
            .filter { it.index != 0u }
            .distinctBy { it.index }
            .sortedBy { it.index }
            .map { comp ->
                Variable(
                    comp.variableName,
                    null,
                    // TODO for choice, override com.intellij.codeInsight.template.Expression::calculateLookupItems
                    comp.placeholder?.let { TextExpression(it) },
                    true,
                    false
                )
            }
            .forEach(template::addVariable)
    }

    private val LspSnippetComponent.TabStop.variableName get() = "tabStop$index"
}
