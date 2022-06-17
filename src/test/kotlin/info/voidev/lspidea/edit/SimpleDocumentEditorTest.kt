package info.voidev.lspidea.edit

import com.intellij.openapi.editor.Document
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import info.voidev.lspidea.editor.ResolvedTextEdit
import info.voidev.lspidea.editor.applyEdits
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.TextEdit
import org.junit.Before
import org.junit.Test

internal class SimpleDocumentEditorTest {

    private lateinit var document: Document

    @Before
    fun setUp() {
        document = BetterMockDocument()
        document.setText(SRC)
    }

    @Test
    fun `simple insert`() {
        ResolvedTextEdit("foo", 0, 0).applyTo(document)
    }

    @Test
    fun `simple delete`() {
        ResolvedTextEdit("", 1, 3).applyTo(document)
        BasePlatformTestCase.assertSameLines(
            """
            a def
            ABC DEF
        """.trimIndent(), document.text
        )
    }

    @Test
    fun `simple replace`() {
        ResolvedTextEdit("xx", 1, 3).applyTo(document)
        BasePlatformTestCase.assertSameLines(
            """
            axx def
            ABC DEF
        """.trimIndent(), document.text
        )
    }

    @Test
    fun `replace with longer`() {
        ResolvedTextEdit("xxyy", 1, 3).applyTo(document)
        BasePlatformTestCase.assertSameLines(
            """
            axxyy def
            ABC DEF
        """.trimIndent(), document.text
        )
    }

    @Test
    fun `multiple replace`() {
        document.applyEdits(listOf(
            TextEdit(Range(Position(0, 5), Position(0, 6)), "56"),
            TextEdit(Range(Position(0, 1), Position(0, 3)), "1234"),
        ))
        BasePlatformTestCase.assertSameLines(
            """
            a1234 d56f
            ABC DEF
        """.trimIndent(), document.text
        )
    }

    @Test
    fun `multiple replace (consecutive)`() {
        document.applyEdits(listOf(
            TextEdit(Range(Position(0, 3), Position(0, 7)), "56"),
            TextEdit(Range(Position(0, 1), Position(0, 3)), "1234"),
        ))
        BasePlatformTestCase.assertSameLines(
            """
            a123456
            ABC DEF
        """.trimIndent(), document.text
        )
    }

    @Test
    fun `multiple inserts at same offset`() {
        // According to the LSP specification:
        // > If multiple inserts have the same position,
        // > the order in the array defines the order in which
        // > the inserted strings appear in the resulting text.
        document.setText("""
            abc
        """.trimIndent())
        document.applyEdits(listOf(
            TextEdit(Range(Position(0, 2), Position(0, 2)), "1"),
            TextEdit(Range(Position(0, 2), Position(0, 2)), "2"),
        ))
        BasePlatformTestCase.assertSameLines(
            """
            ab12c
        """.trimIndent(), document.text
        )
    }

    companion object {
        private val SRC = """
            abc def
            ABC DEF
        """.trimIndent()
    }
}
