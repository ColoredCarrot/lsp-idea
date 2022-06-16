package info.voidev.lspidea.snippet

import it.unimi.dsi.fastutil.shorts.ShortArraySet

/**
 * A lexer that tokenizes a string into [LspSnippetToken]s.
 * Never throws on invalid input ("all input is valid"-philosophy).
 */
open class LspSnippetLexer(private val src: CharSequence) : Iterator<LspSnippetToken> {

    private var i = 0
    private var insideElem = false

    override fun hasNext() = i < src.length

    override fun next(): LspSnippetToken {
        val res = lexNext()
        when (res) {
            is LspSnippetToken.ElemBegin -> insideElem = true
            is LspSnippetToken.ElemEnd -> insideElem = false
            else -> {}
        }
        return res
    }

    private fun lexNext(): LspSnippetToken {
        if (!hasNext()) throw NoSuchElementException()

        if (insideElem) return parseInsideElem()

        tryParseElem()?.also { return it }

        val from = i
        do ++i
        while (i < src.length && !atElem())

        return LspSnippetToken.Text(src.substring(from, i).replace("\\\\", "\\").replace("\\\$", "\$"))
    }

    private fun atElem(): Boolean {
        val i = this.i
        val isAtElem = tryParseElem() != null
        this.i = i
        return isAtElem
    }

    /**
     * Attempts to parse an element ($1 or ${).
     * If successful, sets [i] to one after
     * and returns the token;
     * otherwise, [i] is left unchanged.
     */
    private fun tryParseElem(): LspSnippetToken? {
        if (countPrecedingBackslashes() % 2 == 1) {
            return null
        }

        val origI = i
        if (src[i] == '$' && i + 1 < src.length) {
            ++i
            when (src[i]) {
                '{' -> {
                    ++i
                    return LspSnippetToken.ElemBegin
                }
                in '0'..'9' -> return LspSnippetToken.SimpleElem(parseInt())
            }
        }

        i = origI
        return null
    }

    private fun parseInsideElem(): LspSnippetToken {
        when (src[i]) {
            in '0'..'9' -> {
                return LspSnippetToken.Integer(parseInt())
            }
            '}' -> {
                ++i
                return LspSnippetToken.ElemEnd
            }
            '|' -> {
                ++i
                return LspSnippetToken.Pipe
            }
            ':' -> {
                ++i
                return LspSnippetToken.Colon
            }
            ',' -> {
                ++i
                return LspSnippetToken.Comma
            }
            else -> {
                // Just normal text
                // TODO handle escape sequences
                val from = i
                do ++i
                while (i < src.length && src[i].code.toShort() !in SPECIAL_CHARS_IN_ELEMENTS)
                return LspSnippetToken.Text(src.substring(from, i))
            }
        }
    }

    private fun countPrecedingBackslashes(): Int {
        var res = 0
        var off = 1
        while (i - off > 0 && src[i - off] == '\\') {
            ++res
            ++off
        }
        return res
    }

    private fun parseInt(): UInt {
        assert(i < src.length && src[i] in '0'..'9') { "Got ${src[i]}" }

        val from = i
        var value = 0u
        do {
            value = 10u * value + (src[i] - '0').toUInt()
            ++i
        } while (i < src.length && src[i] in '0'..'9')

        // i is now one past the last digit
        return value
    }

    companion object {
        fun lex(src: CharSequence) = LspSnippetLexer(src).asSequence().toList()

        private val SPECIAL_CHARS_IN_ELEMENTS = ShortArraySet.ofUnchecked(
            '}'.code.toShort(), '|'.code.toShort(), ':'.code.toShort(), ','.code.toShort()
        )
    }
}
