package info.voidev.lspidea.snippet

sealed class LspSnippetComponent {

    class Text(val string: String) : LspSnippetComponent()

    /**
     * TODO: LSP defines that placeholders can be nested.
     *  Find a use-case and extend this if necessary.
     */
    class TabStop(
        val index: UInt,
        val placeholder: String?, // TODO: placeholder can also hold a list of choices
    ) : LspSnippetComponent()

    class Variable(
        val identifier: LspSnippetKnownVariable,
        val default: String?,
        //TODO: val transform
    )

}
