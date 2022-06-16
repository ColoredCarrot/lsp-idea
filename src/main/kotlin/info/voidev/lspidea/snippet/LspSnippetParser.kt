package info.voidev.lspidea.snippet

import org.jetbrains.annotations.Contract

/**
 * Grammar:
 * ```ebnf
 *  any         ::= tabstop | placeholder | choice | variable | text
 *  tabstop     ::= '$' int | '${' int '}'
 *  placeholder ::= '${' int ':' any '}'
 *  choice      ::= '${' int '|' text (',' text)* '|}'
 *  variable    ::= '$' var | '${' var }'
 *                | '${' var ':' any '}'
 *                | '${' var '/' regex '/' (format | text)+ '/' options '}'
 *  format      ::= '$' int | '${' int '}'
 *                | '${' int ':' '/upcase' | '/downcase' | '/capitalize' '}'
 *                | '${' int ':+' if '}'
 *                | '${' int ':?' if ':' else '}'
 *                | '${' int ':-' else '}' | '${' int ':' else '}'
 *  regex       ::= Regular Expression value (ctor-string)
 *  options     ::= Regular Expression option (ctor-options)
 *  var         ::= [_a-zA-Z] [_a-zA-Z0-9]*
 *  int         ::= [0-9]+
 *  text        ::= .*
 * ```
 */
class LspSnippetParser(private val tokens: Iterator<LspSnippetToken>) : Iterator<LspSnippetComponent> {

    override fun hasNext() = tokens.hasNext()

    override fun next(): LspSnippetComponent {
        if (!tokens.hasNext()) throw NoSuchElementException()

        return when (val tok = tokens.next()) {
            is LspSnippetToken.Text -> LspSnippetComponent.Text(tok.string)
            is LspSnippetToken.SimpleElem -> LspSnippetComponent.TabStop(tok.value, null)
            LspSnippetToken.ElemBegin -> nextElem()
            else -> parseError("Unexpected token: $tok")
        }
    }

    private fun needsNext(msg: String): LspSnippetToken {
        if (!tokens.hasNext()) parseError(msg)
        return tokens.next()
    }

    private inline fun <reified T : LspSnippetToken> expect(msg: String): T {
        val tok = needsNext(msg)
        if (tok !is T) parseError(msg)
        return tok
    }

    private fun nextElem(): LspSnippetComponent {
        fun needsNext() = needsNext("Unmatched \${")

        when (val firstTok = needsNext()) {
            is LspSnippetToken.Integer -> {
                // Could be a simple tab stop ${1},
                // a placeholder ${1:foo},
                // or a choice ${1|foo,bar|}
                return when (needsNext()) {
                    is LspSnippetToken.ElemEnd -> {
                        LspSnippetComponent.TabStop(firstTok.value, null)
                    }
                    is LspSnippetToken.Colon -> {
                        val placeholder = expect<LspSnippetToken.Text>("Expected placeholder after colon")
                        endElem(LspSnippetComponent.TabStop(firstTok.value, placeholder.string))
                    }
                    is LspSnippetToken.Pipe -> {
                        TODO("choice")
                    }
                    else -> parseError("Invalid tab stop syntax")
                }
            }
            else -> parseError("Unexpected first token in \${...}: $firstTok")
        }
    }

    @Contract("_ -> retVal")
    private fun endElem(retVal: LspSnippetComponent): LspSnippetComponent {
        if (needsNext("Unmatched \${") !== LspSnippetToken.ElemEnd) {
            parseError("Expected closing token")
        }

        return retVal
    }

    private fun parseError(msg: String): Nothing = throw LspSnippetParseException(msg)
}
