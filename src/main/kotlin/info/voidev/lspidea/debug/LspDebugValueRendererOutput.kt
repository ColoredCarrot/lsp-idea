package info.voidev.lspidea.debug

interface LspDebugValueRendererOutput {

    fun append(value: Any?)

    fun appendNumeric(value: String)

    fun appendString(value: String)

    fun appendKeyword(keyword: String)

    fun appendText(text: String)

    fun appendComment(comment: String)

    fun appendError(error: String)
}
