package info.voidev.lspidea.edit

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import info.voidev.lspidea.editor.ResolvedTextEdit
import info.voidev.lspidea.editor.applyEdits
import info.voidev.lspidea.misc.LspFileType
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.TextEdit
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class DocumentEditorTest : BasePlatformTestCase() {

    override fun getTestDataPath() = "src/test/testdata"

    override fun isWriteActionRequired() = true

    @Test
    fun `simple insert`() {
        myFixture.configureByText(LspFileType, SRC)
        ResolvedTextEdit("foo", 0, 0).applyTo(myFixture.editor.document)
    }

    @Test
    fun `simple delete`() {
        myFixture.configureByText(LspFileType, SRC)
        ResolvedTextEdit("", 1, 3).applyTo(myFixture.editor.document)
        assertSameLines("""
            a def
            ABC DEF
        """.trimIndent(), myFixture.editor.document.text)
    }

    @Test
    fun `simple replace`() {
        myFixture.configureByText(LspFileType, SRC)
        ResolvedTextEdit("xx", 1, 3).applyTo(myFixture.editor.document)
        assertSameLines("""
            axx def
            ABC DEF
        """.trimIndent(), myFixture.editor.document.text)
    }

    @Test
    fun `replace with longer`() {
        myFixture.configureByText(LspFileType, SRC)
        ResolvedTextEdit("xxyy", 1, 3).applyTo(myFixture.editor.document)
        assertSameLines("""
            axxyy def
            ABC DEF
        """.trimIndent(), myFixture.editor.document.text)
    }

    @Test
    fun `multiple replace`() {
        myFixture.configureByText(LspFileType, SRC)
        myFixture.editor.document.applyEdits(listOf(
            TextEdit(Range(Position(0, 5), Position(0, 6)), "56"),
            TextEdit(Range(Position(0, 1), Position(0, 3)), "1234"),
        ))
        assertSameLines("""
            a1234 d56f
            ABC DEF
        """.trimIndent(), myFixture.editor.document.text)
    }

    @Test
    fun `multiple replace (consecutive)`() {
        myFixture.configureByText(LspFileType, SRC)
        myFixture.editor.document.applyEdits(listOf(
            TextEdit(Range(Position(0, 3), Position(0, 7)), "56"),
            TextEdit(Range(Position(0, 1), Position(0, 3)), "1234"),
        ))
        assertSameLines("""
            a123456
            ABC DEF
        """.trimIndent(), myFixture.editor.document.text)
    }

    @Test
    fun `multiple inserts at same offset`() {
        // According to the LSP specification:
        // > If multiple inserts have the same position,
        // > the order in the array defines the order in which
        // > the inserted strings appear in the resulting text.
        myFixture.configureByText(LspFileType, """
            abc
        """.trimIndent())
        myFixture.editor.document.applyEdits(listOf(
            TextEdit(Range(Position(0, 2), Position(0, 2)), "1"),
            TextEdit(Range(Position(0, 2), Position(0, 2)), "2"),
        ))
        assertSameLines("""
            ab12c
        """.trimIndent(), myFixture.editor.document.text)
    }

    companion object {
        private val SRC = """
            abc def
            ABC DEF
        """.trimIndent()
    }
}
