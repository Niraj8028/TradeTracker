package com.wallstreet.domain.insights.detectors

import com.wallstreet.domain.model.TradeType
import com.wallstreet.domain.model.insights.InsightSeverity
import com.wallstreet.testutil.InsightContexts
import com.wallstreet.testutil.TradeFixtures.losers
import com.wallstreet.testutil.TradeFixtures.trade
import com.wallstreet.testutil.TradeFixtures.winners
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MistakeDetectorsTest {

    private fun historyAnchor() = trade(pnl = 5.0, daysAgo = 80)

    @Test
    fun `costliest mistake fires with the loss figure and the preserved advice`() {
        val trades = losers(6, each = -300.0, mistakes = listOf("No Stop Loss"), daysAgo = 5)
        val out = CostliestMistakeDetector.detect(InsightContexts.of(trades))
        assertEquals(1, out.size)
        val i = out.first()
        assertEquals("mistake.costliest", i.id)
        assertEquals(InsightSeverity.CRITICAL, i.severity) // |−1800| ≥ 2×500
        assertTrue(i.body, i.body.contains("No Stop Loss"))
        assertTrue(i.body, i.body.contains("Decide your exit before you enter."))
        assertTrue(i.priority in 0.0..1.0)
    }

    @Test
    fun `costliest mistake stays silent below the minimum sample`() {
        val trades = losers(2, mistakes = listOf("FOMO"), daysAgo = 5)
        assertTrue(CostliestMistakeDetector.detect(InsightContexts.of(trades)).isEmpty())
    }

    @Test
    fun `movement detectors are silent without a previous window`() {
        val ctx = InsightContexts.of(losers(5, mistakes = listOf("FOMO"), daysAgo = 5))
        assertNull(ctx.previous)
        assertTrue(RepeatingMistakeDetector.detect(ctx).isEmpty())
        assertTrue(ReducingMistakeDetector.detect(ctx).isEmpty())
        assertTrue(ClearedMistakeDetector.detect(ctx).isEmpty())
        assertTrue(NewMistakeDetector.detect(ctx).isEmpty())
    }

    @Test
    fun `repeating fires when a losing mistake is not improving`() {
        val trades = losers(3, each = -100.0, mistakes = listOf("FOMO"), daysAgo = 45) +
            losers(4, each = -100.0, mistakes = listOf("FOMO"), daysAgo = 5) +
            historyAnchor()
        val out = RepeatingMistakeDetector.detect(InsightContexts.of(trades))
        assertEquals(listOf("mistake.repeating:FOMO"), out.map { it.id })
        assertTrue(out.first().body, out.first().body.contains("from 3x to 4x"))
    }

    @Test
    fun `reducing fires when a mistake drops by at least a third`() {
        val trades = losers(6, each = -50.0, mistakes = listOf("FOMO"), daysAgo = 45) +
            losers(2, each = -50.0, mistakes = listOf("FOMO"), daysAgo = 5) +
            historyAnchor()
        val out = ReducingMistakeDetector.detect(InsightContexts.of(trades))
        assertEquals(listOf("mistake.reducing:FOMO"), out.map { it.id })
        assertTrue(out.first().body.contains("from 6x to 2x"))
        assertEquals(InsightSeverity.POSITIVE, out.first().severity)
    }

    @Test
    fun `cleared fires when a repeated mistake goes to zero`() {
        val trades = losers(4, mistakes = listOf("Revenge Trade"), daysAgo = 45) +
            winners(5, daysAgo = 5) +
            historyAnchor()
        val out = ClearedMistakeDetector.detect(InsightContexts.of(trades))
        assertEquals(listOf("mistake.cleared:Revenge Trade"), out.map { it.id })
    }

    @Test
    fun `new fires for a mistake that only appears this period`() {
        val trades = winners(4, daysAgo = 45) +
            losers(3, each = -80.0, mistakes = listOf("Large Size"), daysAgo = 5) +
            historyAnchor()
        val out = NewMistakeDetector.detect(InsightContexts.of(trades))
        assertEquals(listOf("mistake.new:Large Size"), out.map { it.id })
        assertTrue(out.first().body.contains("showed up for the first time"))
    }

    @Test
    fun `clean edge fires when disciplined trades clearly outperform tagged ones`() {
        val trades = winners(6, daysAgo = 5) +
            losers(2, each = -10.0, daysAgo = 5) +
            listOf(trade(pnl = 10.0, mistakes = listOf("FOMO"), daysAgo = 6)) +
            losers(5, each = -10.0, mistakes = listOf("FOMO"), daysAgo = 6)
        val out = CleanVsTaggedEdgeDetector.detect(InsightContexts.of(trades))
        assertEquals("mistake.cleanEdge", out.single().id)
        assertEquals(InsightSeverity.POSITIVE, out.single().severity)
    }

    @Test
    fun `concentration fires when one mistake dominates the flagged trades`() {
        val trades = losers(5, each = -20.0, mistakes = listOf("No Setup"), daysAgo = 5) +
            losers(2, each = -20.0, mistakes = listOf("FOMO"), daysAgo = 5)
        val out = MistakeConcentrationDetector.detect(InsightContexts.of(trades))
        assertEquals("mistake.concentration", out.single().id)
        assertTrue(out.single().body.contains("No Setup"))
    }

    @Test
    fun `do-not-enter fires for a tag whose trades rarely win`() {
        val trades = listOf(trade(pnl = 50.0, mistakes = listOf("FOMO"), daysAgo = 5)) +
            losers(4, each = -30.0, mistakes = listOf("FOMO"), daysAgo = 5)
        val out = DoNotEnterMistakeDetector.detect(InsightContexts.of(trades))
        assertEquals(listOf("mistake.doNotEnter:FOMO"), out.map { it.id })
    }

    @Test
    fun `overall mistake rate fires when it jumps versus last period`() {
        val prev = winners(8, daysAgo = 45) +
            losers(2, mistakes = listOf("FOMO"), daysAgo = 45)
        val cur = winners(4, daysAgo = 5) +
            losers(6, each = -10.0, mistakes = listOf("FOMO"), daysAgo = 5)
        val out = OverallMistakeRateDetector.detect(InsightContexts.of(prev + cur + historyAnchor()))
        assertEquals("mistake.overallRate", out.single().id)
        assertEquals(InsightSeverity.WARNING, out.single().severity)
    }

    @Test
    fun `a short trade tag still resolves`() {
        val trades = losers(4, each = -100.0, mistakes = listOf("SL Trailed"), daysAgo = 5)
            .map { it.copy(tradeType = TradeType.SHORT) }
        val out = CostliestMistakeDetector.detect(InsightContexts.of(trades))
        assertEquals("SL Trailed", out.single().title)
    }
}
