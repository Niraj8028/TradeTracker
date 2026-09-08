package com.wallstreet.core.util

import com.wallstreet.domain.model.Trade
import kotlin.math.max

/** Result of a peak-to-trough drawdown scan: [amount] is a positive number. */
data class DrawdownResult(val amount: Double, val atEpochMillis: Long?)

/**
 * Pure aggregate math over a list of [Trade]s, shared by the insights engine and the strategy
 * screens. "Win" is canonically `profitLoss > 0`; open trades (`profitLoss == null`) are
 * ignored. Every function is O(n) and side-effect free — no Android or coroutine dependencies.
 */
object TradeMath {

    private fun closedSorted(trades: List<Trade>): List<Trade> =
        trades.filter { it.profitLoss != null }.sortedBy { it.tradeDate }

    fun grossProfit(trades: List<Trade>): Double =
        trades.mapNotNull { it.profitLoss }.filter { it > 0.0 }.sum()

    /** Total losing P&L, returned as a positive number. */
    fun grossLoss(trades: List<Trade>): Double =
        trades.mapNotNull { it.profitLoss }.filter { it < 0.0 }.sumOf { -it }

    /** grossProfit / grossLoss, or `null` when there are no losses. */
    fun profitFactor(trades: List<Trade>): Double? {
        val loss = grossLoss(trades)
        if (loss <= 0.0) return null
        return grossProfit(trades) / loss
    }

    fun avgWin(trades: List<Trade>): Double {
        val wins = trades.mapNotNull { it.profitLoss }.filter { it > 0.0 }
        return if (wins.isEmpty()) 0.0 else wins.sum() / wins.size
    }

    /** Mean losing P&L, returned as a positive number. */
    fun avgLoss(trades: List<Trade>): Double {
        val losses = trades.mapNotNull { it.profitLoss }.filter { it < 0.0 }
        return if (losses.isEmpty()) 0.0 else losses.sumOf { -it } / losses.size
    }

    /** 0..100 over closed trades; win == `profitLoss > 0`. */
    fun winRate(trades: List<Trade>): Double {
        val closed = trades.mapNotNull { it.profitLoss }
        if (closed.isEmpty()) return 0.0
        return closed.count { it > 0.0 }.toDouble() / closed.size * 100.0
    }

    /** winRate * avgWin − lossRate * avgLoss, per closed trade. */
    fun expectancy(trades: List<Trade>): Double {
        val closed = trades.mapNotNull { it.profitLoss }
        if (closed.isEmpty()) return 0.0
        val n = closed.size.toDouble()
        val winP = closed.count { it > 0.0 } / n
        val lossP = closed.count { it < 0.0 } / n
        return winP * avgWin(trades) - lossP * avgLoss(trades)
    }

    /** Longest chronological run of consecutive wins. Breakeven / open trades don't reset it. */
    fun winStreak(trades: List<Trade>): Int = longestRun(closedSorted(trades)) { it > 0.0 }

    /** Longest chronological run of consecutive losses. Breakeven / open trades don't reset it. */
    fun lossStreak(trades: List<Trade>): Int = longestRun(closedSorted(trades)) { it < 0.0 }

    private inline fun longestRun(sorted: List<Trade>, predicate: (Double) -> Boolean): Int {
        var current = 0
        var longest = 0
        for (t in sorted) {
            val pnl = t.profitLoss ?: 0.0
            when {
                predicate(pnl) -> {
                    current++
                    longest = max(longest, current)
                }
                pnl != 0.0 -> current = 0 // opposite sign resets; breakeven is a no-op
            }
        }
        return longest
    }

    /**
     * Signed run ending at the most recent closed trade: `+n` consecutive wins, `−n`
     * consecutive losses, `0` when the latest closed trade is breakeven or there are none.
     */
    fun currentStreak(trades: List<Trade>): Int {
        val sorted = closedSorted(trades)
        if (sorted.isEmpty()) return 0
        val sign = when {
            (sorted.last().profitLoss ?: 0.0) > 0.0 -> 1
            (sorted.last().profitLoss ?: 0.0) < 0.0 -> -1
            else -> return 0
        }
        var n = 0
        for (t in sorted.asReversed()) {
            val pnl = t.profitLoss ?: 0.0
            if ((sign == 1 && pnl > 0.0) || (sign == -1 && pnl < 0.0)) n++ else break
        }
        return n * sign
    }

    /**
     * Max peak-to-trough dip in cumulative P&L, as a positive [DrawdownResult.amount], plus the
     * `tradeDate` of the trough. Peak is anchored at 0 (starting equity), matching the existing
     * strategy-detail math.
     */
    fun maxDrawdown(trades: List<Trade>): DrawdownResult {
        var equity = 0.0
        var peak = 0.0
        var maxDd = 0.0
        var atMs: Long? = null
        for (t in closedSorted(trades)) {
            equity += t.profitLoss ?: 0.0
            peak = max(peak, equity)
            val dd = peak - equity
            if (dd > maxDd) {
                maxDd = dd
                atMs = t.tradeDate
            }
        }
        return DrawdownResult(maxDd, atMs)
    }

    /**
     * Max peak-to-trough dip over a precomputed cumulative series (peak anchored at the first
     * point, or 0 when empty).
     */
    fun maxDrawdownFromSeries(cumulative: List<Double>): Double {
        if (cumulative.isEmpty()) return 0.0
        var peak = cumulative.first()
        var maxDd = 0.0
        for (v in cumulative) {
            peak = max(peak, v)
            maxDd = max(maxDd, peak - v)
        }
        return maxDd
    }
}
