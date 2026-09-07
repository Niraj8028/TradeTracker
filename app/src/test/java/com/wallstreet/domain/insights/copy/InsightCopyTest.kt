package com.wallstreet.domain.insights.copy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class InsightCopyTest {

    @Test
    fun `render substitutes every token and normalises whitespace`() {
        val out = InsightCopy.render(
            "mistake.reducing",
            mapOf("mistake" to "FOMO", "prev" to "6", "count" to "2"),
        )
        assertEquals("You cut FOMO from 6× to 2× — keep that going.", out)
    }

    @Test
    fun `render collapses the gap left by an empty advice token`() {
        val out = InsightCopy.render(
            "mistake.costliest",
            mapOf(
                "mistake" to "FOMO", "impact" to "-$100", "count" to "3",
                "tradeWord" to "trades", "advice" to "",
            ),
        )
        assertTrue(out.endsWith("your single most expensive leak."))
        assertTrue("  " !in out)
    }

    @Test
    fun `advice returns the preserved paragraph for a known mistake`() {
        assertEquals(
            "Trading without a stop exposes you to uncapped losses. Define your exit before you enter the trade.",
            InsightCopy.advice("No Stop Loss"),
        )
    }

    @Test
    fun `advice falls back for an unknown mistake`() {
        val out = InsightCopy.advice("Chasing News")
        assertTrue(out.contains("Chasing News"))
        assertTrue(out.contains("write one rule"))
    }

    @Test
    fun `unknown template key throws`() {
        assertThrows(IllegalStateException::class.java) {
            InsightCopy.render("does.not.exist", emptyMap())
        }
    }
}
