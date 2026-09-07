package com.wallstreet.domain.insights.copy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class InsightCopyTest {

    @Test
    fun `render substitutes every token`() {
        val out = InsightCopy.render(
            "mistake.reducing",
            mapOf("mistake" to "FOMO", "prev" to "6", "count" to "2"),
        )
        assertEquals("FOMO is down from 6x to 2x. Keep it going.", out)
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
        assertTrue(out, out.endsWith("across 3 trades."))
        assertTrue(out, "  " !in out)
    }

    @Test
    fun `advice returns the coaching line for a known mistake`() {
        assertEquals(
            "With no stop the loss has no floor. Decide your exit before you enter.",
            InsightCopy.advice("No Stop Loss"),
        )
    }

    @Test
    fun `advice falls back for an unknown mistake`() {
        val out = InsightCopy.advice("Chasing News")
        assertTrue(out.contains("Chasing News"))
        assertTrue(out.contains("one rule"))
    }

    @Test
    fun `unknown template key throws`() {
        assertThrows(IllegalStateException::class.java) {
            InsightCopy.render("does.not.exist", emptyMap())
        }
    }
}
