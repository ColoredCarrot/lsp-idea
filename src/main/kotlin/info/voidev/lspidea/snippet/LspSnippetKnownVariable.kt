package info.voidev.lspidea.snippet

enum class LspSnippetKnownVariable {
    /** The currently selected text or the empty string */
    TM_SELECTED_TEXT,
    /** The contents of the current line */
    TM_CURRENT_LINE,
    /** The contents of the word under cursor or the empty string */
    TM_CURRENT_WORD,
    /** The zero-index based line number */
    TM_LINE_INDEX,
    /** The one-index based line number */
    TM_LINE_NUMBER,
    /** The filename of the current document */
    TM_FILENAME,
    /** The filename of the current document without its extensions */
    TM_FILENAME_BASE,
    /** The directory of the current document */
    TM_DIRECTORY,
    /** TM_FILEPATH */
    TM_FILEPATH,
}
