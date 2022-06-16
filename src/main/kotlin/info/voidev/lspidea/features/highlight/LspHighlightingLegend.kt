package info.voidev.lspidea.features.highlight

import org.eclipse.lsp4j.SemanticTokensLegend

@JvmInline
value class LspHighlightingLegend(private val legend: SemanticTokensLegend?) {

    fun resolve(encoded: Int) = legend?.tokenTypes?.getOrNull(encoded)

    fun resolveMods(encoded: Int): List<String> {
        val allMods: List<String> = legend?.tokenModifiers ?: return emptyList()
        return allMods.filterIndexed { i, _ ->
            val mask = 1 shl i
            mask and encoded == mask
        }
    }
}
