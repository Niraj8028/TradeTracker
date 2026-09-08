package com.wallstreet.domain.usecase.insights

import com.wallstreet.domain.model.TimePeriod
import com.wallstreet.testutil.InsightContexts
import com.wallstreet.testutil.TradeFixtures.losers
import com.wallstreet.testutil.TradeFixtures.trade
import com.wallstreet.testutil.TradeFixtures.winners
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildInsightContextUseCaseTest {

    @Test
    fun `trades are split into current and previous windows`() {
        val trades = winners(5, daysAgo = 5) +
            winners(4, daysAgo = 45) +
            trade(pnl = 1.0, daysAgo = 80)
        val ctx = InsightContexts.of(trades)
        assertEquals(5, ctx.current.closedCount)
        assertNotNull(ctx.previous)
        assertEquals(4, ctx.previous!!.closedCount)
    }

    @Test
    fun `previous is null below the minimum prior-window trade count`() {
        val trades = winners(5, daysAgo = 5) +
            winners(2, daysAgo = 45) +
            trade(pnl = 1.0, daysAgo = 80)
        assertNull(InsightContexts.of(trades).previous)
    }

    @Test
    fun `previous is null when history does not reach back far enough`() {
        val trades = winners(5, daysAgo = 5) + winners(4, daysAgo = 45)
        assertNull(InsightContexts.of(trades).previous)
    }

    @Test
    fun `ALL period has no previous window`() {
        val trades = winners(10, daysAgo = 5) + winners(10, daysAgo = 400)
        val ctx = InsightContexts.of(trades, period = TimePeriod.ALL)
        assertNull(ctx.previous)
        assertEquals(20, ctx.current.closedCount)
    }

    @Test
    fun `mistake comparisons carry current and previous counts`() {
        val trades = losers(3, mistakes = listOf("FOMO"), daysAgo = 45) +
            losers(5, mistakes = listOf("FOMO"), daysAgo = 5) +
            trade(pnl = 1.0, daysAgo = 80)
        val fomo = InsightContexts.of(trades).mistakeComparisons.single { it.name == "FOMO" }
        assertEquals(5, fomo.count)
        assertEquals(3, fomo.prevCount)
    }

    @Test
    fun `overall mistake rate is tagged trades over closed trades`() {
        val trades = winners(6, daysAgo = 5) +
            losers(4, mistakes = listOf("FOMO"), daysAgo = 5)
        assertEquals(0.4, InsightContexts.of(trades).overallMistakeRate, 1e-9)
    }

    @Test
    fun `roles and currency symbol pass straight through`() {
        val ctx = InsightContexts.of(
            winners(3, daysAgo = 5),
            roles = listOf("Scalping", "Forex"),
            symbol = "₹",
        )
        assertEquals(listOf("Scalping", "Forex"), ctx.roles)
        assertEquals("₹", ctx.currencySymbol)
    }

    @Test
    fun `empty history produces an empty current window and no previous`() {
        val ctx = InsightContexts.of(emptyList())
        assertEquals(0, ctx.current.closedCount)
        assertNull(ctx.previous)
        assertTrue(ctx.mistakeComparisons.isEmpty())
    }
}
