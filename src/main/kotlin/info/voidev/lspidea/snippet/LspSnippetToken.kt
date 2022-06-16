package info.voidev.lspidea.snippet

sealed class LspSnippetToken {

    data class Text(val string: String) : LspSnippetToken()

    data class SimpleElem(val value: UInt) : LspSnippetToken()

    object ElemBegin : LspSnippetToken()
    object ElemEnd : LspSnippetToken()

    // --------------------------------------------------------//
    //  The following tokens will only appear inside elements  //
    // --------------------------------------------------------//

    data class Integer(val value: UInt) : LspSnippetToken()

    object Pipe : LspSnippetToken()
    object Colon : LspSnippetToken()
    object Comma : LspSnippetToken()

}
