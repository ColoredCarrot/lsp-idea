package info.voidev.lspidea.snippet

import it.unimi.dsi.fastutil.ints.IntArraySet

/**
 * A template, or "snippet", as defined by the Language Server Protocol.
 * See [online](https://microsoft.github.io/language-server-protocol/specifications/specification-current/#snippet_syntax).
 */
class LspSnippet(val components: List<LspSnippetComponent>) {
    constructor(snippet: String) : this(LspSnippetParser(LspSnippetLexer(snippet)).asSequence().toList())

    /**
     * The maximum value of [LspSnippetComponent.TabStop.index] (inclusive).
     */
    val maxTabStopIndex: UInt

    val isSimple: Boolean

    init {
        val foundTabStops = IntArraySet(5)
        components.asSequence()
            .filterIsInstance<LspSnippetComponent.TabStop>()
            .filter { it.index != 0u }
            .forEach { foundTabStops.add(it.index.toInt()) }

        // Duplicates are actually allowed
//        if (thereWereDuplicates) {
//            throw LspSnippetParseException("Duplicate tab stop indices are not allowed")
//        }

        // Since the indices must be contiguous,
        // this calculation works as expected
        maxTabStopIndex = foundTabStops.size.toUInt()
        for (index in 1u..maxTabStopIndex) {
            if (index.toInt() !in foundTabStops) {
                throw LspSnippetParseException("Non-contiguous tab stop indices are not allowed")
            }
        }

        // Validate that there is at most one tab stop with index 0
        val zeroIndexTabStopCount = components.asSequence()
            .filterIsInstance<LspSnippetComponent.TabStop>()
            .count { it.index == 0u }
        if (zeroIndexTabStopCount > 1) {
            throw LspSnippetParseException("At most one tab stop can have index 0")
        }

        isSimple = components.all { it is LspSnippetComponent.Text }
    }

}
