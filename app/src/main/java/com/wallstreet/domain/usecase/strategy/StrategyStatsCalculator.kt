package com.wallstreet.domain.usecase.strategy

import com.wallstreet.domain.model.Strategy
import com.wallstreet.domain.model.TimePeriod
import com.wallstreet.domain.model.Trade
import com.wallstreet.domain.model.strategy.StrategyStats

/**
 * Per-strategy aggregate stats over an already-filtered trade list. Pure — no I/O.
 * Extracted from [GetStrategyStatsUsecase] so the insights engine can reuse the exact
 * same computation.
 */
class StrategyStatsCalculator {

    fun compute(
        strategy: Strategy,
        trades: List<Trade>,
        period: TimePeriod,
    ): StrategyStats {
        if (trades.isEmpty()) {
            return StrategyStats(
                strategy = strategy,
                totalTrades = 0,
                totalPnl = 0.0,
                winRate = 0.0,
                rrRatio = 0.0,
                avgProfitPerTrade = 0.0,
                period = period,
            )
        }
        val sorted = trades.sortedBy { it.tradeDate }
        val totalPnl = sorted.sumOf { it.profitLoss ?: 0.0 }
        val wins = sorted.filter { (it.profitLoss ?: 0.0) > 0 }
        val losses = sorted.filter { (it.profitLoss ?: 0.0) < 0 }
        val winRate = wins.size.toDouble() / sorted.size * 100
        val avgWin = if (wins.isEmpty()) 0.0 else wins.sumOf { it.profitLoss ?: 0.0 } / wins.size
        val avgLoss =
            if (losses.isEmpty()) 0.0 else losses.sumOf { -(it.profitLoss ?: 0.0) } / losses.size
        val rrRatio = if (avgLoss > 0) avgWin / avgLoss else 0.0

        // Cumulative P&L over closed trades only — skipping null P&L avoids long
        // flat zero-stretches from open positions that make the chart look fake.
        val sparkline = buildList {
            add(0.0)
            var cumulative = 0.0
            sorted.forEach { trade ->
                val pnl = trade.profitLoss ?: return@forEach
                cumulative += pnl
                add(cumulative)
            }
        }

        return StrategyStats(
            strategy = strategy,
            totalTrades = sorted.size,
            totalPnl = totalPnl,
            winRate = winRate,
            rrRatio = rrRatio,
            avgProfitPerTrade = totalPnl / sorted.size,
            period = period,
            sparkline = sparkline,
        )
    }
}
