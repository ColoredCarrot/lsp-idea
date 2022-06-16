package info.voidev.lspidea.features.highlight

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.BRACES
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.BRACKETS
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.CLASS_NAME
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.CLASS_REFERENCE
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.COMMA
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.CONSTANT
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.DOC_COMMENT
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.DOT
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.FUNCTION_CALL
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.FUNCTION_DECLARATION
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.INSTANCE_FIELD
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.INSTANCE_METHOD
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.KEYWORD
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.LABEL
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.LINE_COMMENT
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.LOCAL_VARIABLE
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.NUMBER
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.OPERATION_SIGN
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.PARAMETER
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.PARENTHESES
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.REASSIGNED_LOCAL_VARIABLE
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.SEMICOLON
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.STATIC_FIELD
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.STATIC_METHOD
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.STRING
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE
import com.intellij.openapi.editor.colors.TextAttributesKey
import org.eclipse.lsp4j.SemanticTokenModifiers as Mods
import org.eclipse.lsp4j.SemanticTokenTypes as Toks

object LspHighlightingMap {
    val SUPPORTED_TOKENS = listOf(
        Toks.Namespace,
        Toks.Type,
        Toks.Class,
        Toks.Enum,
        Toks.Interface,
        Toks.Struct,
        Toks.TypeParameter,
        Toks.Parameter,
        Toks.Variable,
        Toks.Property,
        Toks.EnumMember,
        Toks.Event,
        Toks.Function,
        Toks.Method,
        Toks.Macro,
        Toks.Keyword,
        Toks.Modifier,
        Toks.Comment,
        Toks.String,
        Toks.Number,
        Toks.Regexp,
        Toks.Operator,

        // Non-standard
        "escapeSequence",
        "label",
        "brace",
        "bracket",
        "dot",
        "comma",
        "parenthesis", "parentheses",
        "semicolon",
        "typeAlias",
    )

    val SUPPORTED_MODS = listOf(
        Mods.Declaration,
        Mods.Definition,
        Mods.Static,
        Mods.Readonly,
        Mods.Documentation,
    )

    fun findStyle(token: String, mods: Iterable<String>): TextAttributesKey? {
        val decldef = Mods.Declaration in mods || Mods.Definition in mods
        val static = Mods.Static in mods
        return when (token) {
            Toks.Comment -> if (Mods.Documentation in mods) DOC_COMMENT else LINE_COMMENT
            Toks.Keyword -> KEYWORD
            Toks.String -> STRING
            Toks.Number -> NUMBER
            Toks.Regexp -> STRING // TODO
            Toks.Operator -> OPERATION_SIGN
            Toks.Namespace -> null
            Toks.Type,
            Toks.Struct,
            Toks.Class,
            Toks.Interface,
            Toks.Enum,
            -> if (decldef) CLASS_NAME else CLASS_REFERENCE
            Toks.EnumMember -> CONSTANT
            Toks.Function -> if (decldef) FUNCTION_DECLARATION else FUNCTION_CALL
            Toks.Method -> if (static) STATIC_METHOD else INSTANCE_METHOD
            Toks.Property -> if (static) STATIC_FIELD else INSTANCE_FIELD
            Toks.Macro -> if (decldef) FUNCTION_DECLARATION else FUNCTION_CALL
            Toks.Variable ->
                if (Mods.Readonly in mods) if (static) CONSTANT else LOCAL_VARIABLE
                else if (static) STATIC_FIELD else REASSIGNED_LOCAL_VARIABLE
            Toks.Parameter -> PARAMETER
            Toks.TypeParameter -> PARAMETER // TODO might delegate to JavaHighlightingColors' type parameter
            Toks.Event -> null

            // Non-standard
            "builtinType" -> findStyle(Toks.Type, mods)
            "characterLiteral", "character" -> findStyle(Toks.String, mods)
            "comparison" -> OPERATION_SIGN
            "constParameter" -> PARAMETER
            "escapeSequence" -> VALID_STRING_ESCAPE
            "formatSpecifier" -> NUMBER
            "label" -> LABEL
            "brace" -> BRACES
            "bracket" -> BRACKETS
            "dot" -> DOT
            "comma" -> COMMA
            "parenthesis", "parentheses" -> PARENTHESES
            "colon" -> null
            "punctuation" -> null
            "logical" -> null
            "semicolon" -> SEMICOLON
            "typeAlias" -> CLASS_NAME // TODO might delegate to org.jetbrains.kotlin.idea.highlighter.KotlinHighlightingColors#TYPE_ALIAS

            // rust-analyzer
            "boolean" -> KEYWORD
            "angle",
            "arithmetic",
            "attribute",
            "bitwise",
            "unresolvedReference",
            -> null
            "lifetime" -> LABEL
            "selfKeyword" -> KEYWORD

            else -> null
        }
    }

    /*
    rust-analyzer modifiers
    "documentation",
    "declaration",
    "definition",
    "static",
    "abstract",
    "deprecated",
    "readonly",
    "constant",
    "controlFlow",
    "injected",
    "mutable",
    "consuming",
    "async",
    "unsafe",
    "attribute",
    "trait",
    "callable",
    "intraDocLink"
     */
}
