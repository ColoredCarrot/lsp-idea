package info.voidev.lspidea.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ListSortUtilTest {

    @Test
    fun reverseConsecutiveSequences() {
        val orig = listOf(
            Dummy("a", 1),
            Dummy("b", 7),
            Dummy("c", 3),
            Dummy("d", 3),
            Dummy("e", 2),
            Dummy("f", 2),
        )
        val expected = listOf("a", "b", "d", "c", "f", "e")
        val workList = ArrayList(orig)
        workList.reverseConsecutiveSequences()
        assertEquals(expected, workList.map { it.key })
    }

    @Test
    fun `reverseConsecutiveSequences NOP`() {
        val orig = listOf(
            Dummy("a", 1),
            Dummy("b", 2),
        )
        val expected = listOf("a", "b")
        val workList = ArrayList(orig)
        workList.reverseConsecutiveSequences()
        assertEquals(expected, workList.map { it.key })
    }

    private data class Dummy(val key: String, val v: Int) : Comparable<Dummy> {
        override fun compareTo(other: Dummy) = v.compareTo(other.v)
    }
}
