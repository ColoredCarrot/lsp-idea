package info.voidev.lspidea.features.highlight

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Document
import info.voidev.lspidea.connect.LspSession
import info.voidev.lspidea.util.lspPosition2offset

object LspSemanticTokensParser {

    fun parse(data: List<Int>, document: Document, session: LspSession): List<LspToken> {
        // See https://microsoft.github.io/language-server-protocol/specification#textDocument_semanticTokens

        val legend = session.state.serverCapabilitiesOrNull
            ?.semanticTokensProvider
            ?.legend
            ?.let(::LspHighlightingLegend)
            ?: return emptyList()

        val result = ArrayList<LspToken>(data.size / 5)

        var lastLine = 0
        var lastStart = 0
        for (i in data.indices step 5) {
            if (i + 4 >= data.size) {
                // Invalid data (size not divisible by 5)
                thisLogger().warn("Invalid semantic token data (size not divisible by 5)")
                break
            }

            val deltaLine = data[i]
            val deltaStart = data[i + 1]
            val length = data[i + 2]
            val tokenType = legend.resolve(data[i + 3])
            val tokenMods = legend.resolveMods(data[i + 4])

            val line = lastLine + deltaLine
            val start = if (deltaLine == 0) lastStart + deltaStart else deltaStart
            val offset = document.lspPosition2offset(line, start)

            if (tokenType != null) {
                result += LspToken(tokenType, tokenMods, offset, length)
            } else {
                thisLogger().warn("Unresolved token type: ${data[i + 3]}")
            }

            lastLine = line
            lastStart = start
        }

        return result
    }

}
