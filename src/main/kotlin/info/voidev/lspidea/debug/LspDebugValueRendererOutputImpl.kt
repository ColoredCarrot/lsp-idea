package info.voidev.lspidea.debug

import com.intellij.xdebugger.frame.presentation.XValuePresentation.XValueTextRenderer

class LspDebugValueRendererOutputImpl(private val output: XValueTextRenderer) : LspDebugValueRendererOutput {

    override fun append(value: Any?) {
        if (value == null) appendKeyword("null")
        else LspDebugRendererProvider.findValueRenderer(value.javaClass).render(value, this)
    }

    override fun appendNumeric(value: String) {
        output.renderNumericValue(value)
    }

    override fun appendString(value: String) {
        output.renderStringValue(value)
    }

    override fun appendKeyword(keyword: String) {
        output.renderKeywordValue(keyword)
    }

    override fun appendText(text: String) {
        // TODO potentially better output.renderSpecialSymbol(text)
        output.renderValue(text)
    }

    override fun appendComment(comment: String) {
        output.renderComment(comment)
    }

    override fun appendError(error: String) {
        output.renderError(error)
    }
}
