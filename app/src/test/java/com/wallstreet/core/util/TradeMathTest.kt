package com.wallstreet.core.util

import com.wallstreet.testutil.TradeFixtures.trade
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TradeMathTest {

    private val d = 1e-6

    @Test
    fun `gross profit and loss ignore open trades and split by sign`() {
        val trades = listOf(
            trade(pnl = 100.0), trade(pnl = 40.0),
            trade(pnl = -30.0), trade(pnl = -10.0),
            trade(pnl = null), // open
        )
        assertEquals(140.0, TradeMath.grossProfit(trades), d)
        assertEquals(40.0, TradeMath.grossLoss(trades), d) // positive
    }

    @Test
    fun `profit factor is gross profit over gross loss`() {
        val trades = listOf(trade(pnl = 150.0), trade(pnl = -50.0), trade(pnl = -25.0))
        assertEquals(2.0, TradeMath.profitFactor(trades)!!, d)
    }

    @Test
    fun `profit factor is null with no losing trades`() {
        assertNull(TradeMath.profitFactor(listOf(trade(pnl = 10.0), trade(pnl = 20.0))))
        assertNull(TradeMath.profitFactor(emptyList()))
    }

    @Test
    fun `avg win and avg loss`() {
        val trades = listOf(trade(pnl = 100.0), trade(pnl = 200.0), trade(pnl = -40.0), trade(pnl = -20.0))
        assertEquals(150.0, TradeMath.avgWin(trades), d)
        assertEquals(30.0, TradeMath.avgLoss(trades), d) // positive
        assertEquals(0.0, TradeMath.avgWin(emptyList()), d)
        assertEquals(0.0, TradeMath.avgLoss(emptyList()), d)
    }

    @Test
    fun `win rate is percent of closed trades that are positive`() {
        val trades = listOf(trade(pnl = 10.0), trade(pnl = 10.0), trade(pnl = -10.0), trade(pnl = null))
        assertEquals(2.0 / 3.0 * 100.0, TradeMath.winRate(trades), d) // open trade excluded
        assertEquals(0.0, TradeMath.winRate(emptyList()), d)
    }

    @Test
    fun `expectancy blends win and loss probabilities`() {
        // 2 wins avg +100, 2 losses avg -50, 4 closed => 0.5*100 - 0.5*50 = 25
        val trades = listOf(trade(pnl = 100.0), trade(pnl = 100.0), trade(pnl = -50.0), trade(pnl = -50.0))
        assertEquals(25.0, TradeMath.expectancy(trades), d)
    }

    @Test
    fun `win streak is the longest consecutive run of wins in date order`() {
        // chronological: W W L W W W L  -> longest win run = 3
        val trades = listOf(
            trade(pnl = 10.0, daysAgo = 7),
            trade(pnl = 10.0, daysAgo = 6),
            trade(pnl = -10.0, daysAgo = 5),
            trade(pnl = 10.0, daysAgo = 4),
            trade(pnl = 10.0, daysAgo = 3),
            trade(pnl = 10.0, daysAgo = 2),
            trade(pnl = -10.0, daysAgo = 1),
        )
        assertEquals(3, TradeMath.winStreak(trades))
    }

    @Test
    fun `breakeven trades do not reset a win streak`() {
        val trades = listOf(
            trade(pnl = 10.0, daysAgo = 4),
            trade(pnl = 0.0, daysAgo = 3),
            trade(pnl = 10.0, daysAgo = 2),
        )
        assertEquals(2, TradeMath.winStreak(trades))
    }

    @Test
    fun `loss streak is the longest consecutive run of losses`() {
        val trades = listOf(
            trade(pnl = -10.0, daysAgo = 5),
            trade(pnl = -10.0, daysAgo = 4),
            trade(pnl = -10.0, daysAgo = 3),
            trade(pnl = 10.0, daysAgo = 2),
            trade(pnl = -10.0, daysAgo = 1),
        )
        assertEquals(3, TradeMath.lossStreak(trades))
    }

    @Test
    fun `current streak is signed and anchored to the most recent trade`() {
        val winning = listOf(
            trade(pnl = -10.0, daysAgo = 3),
            trade(pnl = 10.0, daysAgo = 2),
            trade(pnl = 10.0, daysAgo = 1),
        )
        assertEquals(2, TradeMath.currentStreak(winning))

        val losing = listOf(
            trade(pnl = 10.0, daysAgo = 3),
            trade(pnl = -10.0, daysAgo = 2),
            trade(pnl = -10.0, daysAgo = 1),
        )
        assertEquals(-2, TradeMath.currentStreak(losing))

        assertEquals(0, TradeMath.currentStreak(emptyList()))
        assertEquals(0, TradeMath.currentStreak(listOf(trade(pnl = 0.0))))
    }

    @Test
    fun `max drawdown finds the deepest peak to trough dip`() {
        // cumulative: +100, +50 (dd 50), +150 (peak), 0 (dd 150), +60 (dd 90)
        val trades = listOf(
            trade(pnl = 100.0, daysAgo = 5),
            trade(pnl = -50.0, daysAgo = 4),
            trade(pnl = 100.0, daysAgo = 3),
            trade(pnl = -150.0, daysAgo = 2),
            trade(pnl = 60.0, daysAgo = 1),
        )
        val result = TradeMath.maxDrawdown(trades)
        assertEquals(150.0, result.amount, d)
        assertEquals(trades[3].tradeDate, result.atEpochMillis)
    }

    @Test
    fun `max drawdown is zero for an all-winning or empty history`() {
        assertEquals(0.0, TradeMath.maxDrawdown(listOf(trade(pnl = 10.0), trade(pnl = 20.0))).amount, d)
        assertEquals(0.0, TradeMath.maxDrawdown(emptyList()).amount, d)
        assertNull(TradeMath.maxDrawdown(emptyList()).atEpochMillis)
    }

    @Test
    fun `max drawdown from series`() {
        assertEquals(150.0, TradeMath.maxDrawdownFromSeries(listOf(0.0, 100.0, 50.0, 150.0, 0.0, 60.0)), d)
        assertEquals(0.0, TradeMath.maxDrawdownFromSeries(emptyList()), d)
    }

    @Test
    fun `all-open history yields neutral values`() {
        val open = listOf(trade(pnl = null), trade(pnl = null))
        assertEquals(0.0, TradeMath.grossProfit(open), d)
        assertEquals(0.0, TradeMath.grossLoss(open), d)
        assertNull(TradeMath.profitFactor(open))
        assertEquals(0.0, TradeMath.winRate(open), d)
        assertEquals(0.0, TradeMath.expectancy(open), d)
        assertEquals(0, TradeMath.winStreak(open))
        assertEquals(0, TradeMath.currentStreak(open))
    }
}
