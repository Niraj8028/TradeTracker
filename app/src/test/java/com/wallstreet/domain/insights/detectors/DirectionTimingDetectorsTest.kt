package com.wallstreet.domain.insights.detectors

import com.wallstreet.domain.model.TradeType
import com.wallstreet.domain.model.TrendDirection
import com.wallstreet.domain.model.insights.InsightSeverity
import com.wallstreet.testutil.InsightContexts
import com.wallstreet.testutil.TradeFixtures.trade
import com.wallstreet.testutil.TradeFixtures.winners
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DirectionTimingDetectorsTest {

    private fun shorts(n: Int, each: Double, daysAgo: Long = 5) =
        List(n) { trade(pnl = each, type = TradeType.SHORT, daysAgo = daysAgo) }

    @Test
    fun `directional edge fires on a wide long-vs-short win-rate gap`() {
        val trades = winners(6, daysAgo = 5) + shorts(6, each = -50.0)
        val out = DirectionalEdgeDetector.detect(InsightContexts.of(trades))
        assertEquals("direction.edge", out.single().id)
        assertTrue(out.single().body.contains("going long"))
    }

    @Test
    fun `bleeding direction fires when one side is red and the other green`() {
        val trades = winners(6, daysAgo = 5) + shorts(4, each = -80.0)
        val out = BleedingDirectionDetector.detect(InsightContexts.of(trades))
        assertEquals("direction.bleeding", out.single().id)
        assertEquals(InsightSeverity.WARNING, out.single().severity)
        assertTrue(out.single().body.contains("short"))
    }

    @Test
    fun `trend edge fires when one market condition clearly outperforms`() {
        val trades = List(6) { trade(pnl = 100.0, trend = TrendDirection.UP, daysAgo = 5) } +
            List(4) { trade(pnl = -100.0, trend = TrendDirection.DOWN, daysAgo = 5) }
        val out = TrendEdgeDetector.detect(InsightContexts.of(trades))
        assertEquals("trend.edge", out.single().id)
        assertTrue(out.single().body.contains("Uptrend"))
    }

    @Test
    fun `trend losing fires for a condition in the red`() {
        val trades = List(6) { trade(pnl = 100.0, trend = TrendDirection.UP, daysAgo = 5) } +
            List(4) { trade(pnl = -100.0, trend = TrendDirection.DOWN, daysAgo = 5) }
        val out = TrendLosingDetector.detect(InsightContexts.of(trades))
        assertEquals(listOf("trend.losing:DOWN"), out.map { it.id })
    }

    @Test
    fun `best day fires for a strong repeated weekday`() {
        val trades = listOf(
            trade(pnl = 400.0, daysAgo = 7),
            trade(pnl = 400.0, daysAgo = 14),
            trade(pnl = 400.0, daysAgo = 21),
        )
        val out = BestDayDetector.detect(InsightContexts.of(trades))
        assertEquals("timing.bestDay", out.single().id)
        assertEquals(InsightSeverity.POSITIVE, out.single().severity)
    }

    @Test
    fun `worst day fires for a weak repeated weekday`() {
        val trades = listOf(
            trade(pnl = -400.0, daysAgo = 7),
            trade(pnl = -400.0, daysAgo = 14),
            trade(pnl = -400.0, daysAgo = 21),
        )
        val out = WorstDayDetector.detect(InsightContexts.of(trades))
        assertEquals("timing.worstDay", out.single().id)
    }

    @Test
    fun `profit-factor risk fires when losers outweigh winners over 10+ trades`() {
        val trades = winners(3, each = 50.0, daysAgo = 5) +
            List(9) { trade(pnl = -100.0, daysAgo = 5) }
        val out = ProfitFactorRiskDetector.detect(InsightContexts.of(trades))
        assertEquals("risk.profitFactor", out.single().id)
        assertEquals(InsightSeverity.CRITICAL, out.single().severity)
    }

    @Test
    fun `drawdown risk fires on a deep equity dip`() {
        val trades = List(10) { trade(pnl = 100.0, daysAgo = 20) } +
            List(10) { trade(pnl = -100.0, daysAgo = 5) }
        val out = DrawdownRiskDetector.detect(InsightContexts.of(trades))
        assertEquals("risk.drawdown", out.single().id)
    }

    @Test
    fun `loss streak fires after three consecutive losing trades`() {
        val trades = winners(2, daysAgo = 15) + List(4) { trade(pnl = -50.0, daysAgo = 3) }
        val out = LossStreakDetector.detect(InsightContexts.of(trades))
        assertEquals("discipline.lossStreak", out.single().id)
        assertTrue(out.single().body.contains("4 losing trades in a row"))
    }

    @Test
    fun `risk detectors stay silent under 10 closed trades`() {
        val ctx = InsightContexts.of(List(6) { trade(pnl = -100.0, daysAgo = 5) })
        assertTrue(ProfitFactorRiskDetector.detect(ctx).isEmpty())
        assertTrue(DrawdownRiskDetector.detect(ctx).isEmpty())
        assertTrue(RewardRiskDetector.detect(ctx).isEmpty())
    }

    @Test
    fun `reward vs risk warns when wins barely beat losses`() {
        val trades = List(6) { trade(pnl = 120.0, daysAgo = 5) } +
            List(6) { trade(pnl = -100.0, daysAgo = 5) }
        val out = RewardRiskDetector.detect(InsightContexts.of(trades))
        assertEquals("risk.rewardRisk", out.single().id)
        assertEquals(InsightSeverity.WARNING, out.single().severity)
        assertTrue(out.single().body, out.single().body.contains("1.2x"))
    }

    @Test
    fun `reward vs risk is positive when winners are much bigger`() {
        val trades = List(6) { trade(pnl = 300.0, daysAgo = 5) } +
            List(5) { trade(pnl = -100.0, daysAgo = 5) }
        val out = RewardRiskDetector.detect(InsightContexts.of(trades))
        assertEquals(InsightSeverity.POSITIVE, out.single().severity)
    }

    @Test
    fun `reward vs risk is critical when losers are bigger than winners`() {
        val trades = List(4) { trade(pnl = 50.0, daysAgo = 5) } +
            List(6) { trade(pnl = -150.0, daysAgo = 5) }
        val out = RewardRiskDetector.detect(InsightContexts.of(trades))
        assertEquals(InsightSeverity.CRITICAL, out.single().severity)
        assertTrue(out.single().body.contains("bigger than your average win"))
    }
}
