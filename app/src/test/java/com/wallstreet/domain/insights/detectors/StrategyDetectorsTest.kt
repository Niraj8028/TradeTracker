package com.wallstreet.domain.insights.detectors

import com.wallstreet.domain.model.TimePeriod
import com.wallstreet.domain.model.insights.InsightSeverity
import com.wallstreet.domain.model.insights.StrategyInsightContext
import com.wallstreet.domain.model.insights.StrategyVerdict
import com.wallstreet.domain.model.insights.StrategyWindowStats
import com.wallstreet.testutil.TradeFixtures.losers
import com.wallstreet.testutil.TradeFixtures.winners
import com.wallstreet.testutil.strategyWindow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StrategyDetectorsTest {

    private fun ctx(vararg pairs: Pair<StrategyWindowStats, StrategyWindowStats?>) =
        StrategyInsightContext(pairs.toList(), emptyList(), "$", TimePeriod.ONE_MONTH)

    @Test
    fun `scale up picks the profitable high-PF strategy`() {
        val s = strategyWindow("Breakout", winners(8, each = 100.0) + losers(2, each = -50.0))
        val out = ScaleUpStrategyDetector.detect(ctx(s to null))
        assertEquals("strategy.scaleUp:Breakout", out.single().id)
        assertEquals(StrategyVerdict.SCALE_UP, out.single().verdict)
        assertEquals("Breakout", out.single().strategyId)
    }

    @Test
    fun `scale up ignores an unproven strategy`() {
        val s = strategyWindow("Fresh", winners(3, each = 100.0))
        assertTrue(ScaleUpStrategyDetector.detect(ctx(s to null)).isEmpty())
    }

    @Test
    fun `drop fires for a losing sub-1 profit-factor strategy`() {
        val s = strategyWindow("Mean Reversion", winners(3, each = 50.0) + losers(11, each = -100.0))
        val out = DropStrategyDetector.detect(ctx(s to null))
        assertEquals("strategy.drop:Mean Reversion", out.single().id)
        assertEquals(StrategyVerdict.DROP, out.single().verdict)
        assertEquals(InsightSeverity.WARNING, out.single().severity) // |−950| < 3×500
        assertTrue(out.single().body.contains("lost"))
    }

    @Test
    fun `needs data fires between 1 and the minimum sample`() {
        val s = strategyWindow("Gap Fill", winners(2) + losers(2))
        val out = NeedsDataStrategyDetector.detect(ctx(s to null))
        assertEquals(StrategyVerdict.NEEDS_MORE_DATA, out.single().verdict)
    }

    @Test
    fun `declining fires when win rate drops 10pp with enough trades both windows`() {
        val prev = strategyWindow("Scalp", winners(6, each = 40.0) + losers(4, each = -40.0)) // 60%
        val cur = strategyWindow("Scalp", winners(3, each = 40.0) + losers(7, each = -40.0))  // 30%
        val out = DecliningStrategyDetector.detect(ctx(cur to prev))
        assertEquals("strategy.declining:Scalp", out.single().id)
        assertEquals(StrategyVerdict.REVIEW, out.single().verdict)
    }

    @Test
    fun `improving fires when win rate climbs 10pp and the window is green`() {
        val prev = strategyWindow("Swing", winners(3, each = 60.0) + losers(7, each = -20.0)) // 30%
        val cur = strategyWindow("Swing", winners(7, each = 60.0) + losers(3, each = -20.0))  // 70%, +
        val out = ImprovingStrategyDetector.detect(ctx(cur to prev))
        assertEquals("strategy.improving:Swing", out.single().id)
        assertEquals(StrategyVerdict.KEEP, out.single().verdict)
    }

    @Test
    fun `steady fires for a profitable low-drawdown strategy`() {
        val s = strategyWindow("Trend Follow", winners(9, each = 100.0) + losers(1, each = -80.0))
        val out = SteadyStrategyDetector.detect(ctx(s to null))
        assertEquals("strategy.steady:Trend Follow", out.single().id)
        assertEquals(StrategyVerdict.KEEP, out.single().verdict)
    }

    @Test
    fun `declining needs a previous window`() {
        val cur = strategyWindow("Scalp", winners(3) + losers(7))
        assertTrue(DecliningStrategyDetector.detect(ctx(cur to null)).isEmpty())
    }
}
