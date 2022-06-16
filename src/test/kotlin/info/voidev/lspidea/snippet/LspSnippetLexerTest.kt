package info.voidev.lspidea.snippet

import org.junit.Assert.assertEquals
import org.junit.Test
import info.voidev.lspidea.snippet.LspSnippetToken as Tok

internal class LspSnippetLexerTest {

    private fun assertLexing(src: CharSequence, vararg expected: Any) {
        assertEquals(expected.map {
            when (it) {
                is Tok -> it
                is UInt -> Tok.SimpleElem(it)
                is String -> Tok.Text(it)
                else -> error("Illegal token specifier: $it")
            }
        }, LspSnippetLexer.lex(src))
    }

    @Test
    fun `basic snippet`() {
        assertLexing("foo$1bar", "foo", 1u, "bar")
    }

    @Test
    fun `escaped elem`() {
        assertLexing("foo $23 bar \\\\\\\$0", "foo ", 23u, " bar \\\$0")
    }

    @Test
    fun `multiple tab stops`() {
        assertLexing("$2$1 $5", 2u, 1u, " ", 5u)
    }

    @Test
    fun `'all input is valid'-philosophy`() {
        assertLexing("foo\$bar$$0", "foo\$bar\$", 0u)
    }

    @Test
    fun `no special symbols outside elements`() {
        assertLexing("foo|bar \${|foo,} ,ba:z", "foo|bar ", Tok.ElemBegin, Tok.Pipe, "foo", Tok.Comma, Tok.ElemEnd, " ,ba:z")
    }

}
