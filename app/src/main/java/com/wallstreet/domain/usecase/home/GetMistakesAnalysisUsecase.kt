package com.wallstreet.domain.usecase.home

import com.wallstreet.core.constants.AppConstants
import com.wallstreet.domain.model.MistakeStat
import com.wallstreet.domain.model.MistakesAnalysisData
import com.wallstreet.domain.model.Trade

class GetMistakesAnalysisUsecase {
    operator fun invoke(trades: List<Trade>): MistakesAnalysisData {
        return calculateMistakeAnalytics(trades)
    }

    private fun calculateMistakeAnalytics(trades: List<Trade>): MistakesAnalysisData {
        val closedTrades = trades.filter { it.profitLoss != null }

        if (closedTrades.isEmpty()) {
            return MistakesAnalysisData(
                topMistakes = emptyList(),
                mostCostlyMistakes = MistakeStat("", 0, 0.0, 0.0, 0.0),
                totalMistakeTrades = 0,
                cleanTradeWinRate = 0.0
            )
        }

        val mistakeStats = AppConstants.mistakes
            .map { mistake -> buildStatFor(mistake, closedTrades) }
            .filter { it.count > 0 }
            .sortedByDescending { it.count }

        val mostCostlyMistake = mistakeStats.minByOrNull { it.totalPnlImpact }
            ?: MistakeStat("", 0, 0.0, 0.0, 0.0)

        val totalMistakeTrades = closedTrades.count { it.mistakes.isNotEmpty() }

        val cleanTrades = closedTrades.filter { it.mistakes.isEmpty() }
        val cleanWins = cleanTrades.count { (it.profitLoss ?: 0.0) >= 0.0 }
        val cleanTradeWinRate = if (cleanTrades.isEmpty()) 0.0
            else (cleanWins.toDouble() / cleanTrades.size) * 100.0

        return MistakesAnalysisData(
            topMistakes = mistakeStats,
            mostCostlyMistakes = mostCostlyMistake,
            totalMistakeTrades = totalMistakeTrades,
            cleanTradeWinRate = cleanTradeWinRate
        )
    }

    private fun buildStatFor(mistake: String, closedTrades: List<Trade>): MistakeStat {
        val tagged = closedTrades.filter { mistake in it.mistakes }
        val count = tagged.size
        val totalPnlImpact = tagged.sumOf { it.profitLoss ?: 0.0 }
        val avgPnlImpact = if (count == 0) 0.0 else totalPnlImpact / count
        val wins = tagged.count { (it.profitLoss ?: 0.0) >= 0.0 }
        val winRate = if (count == 0) 0.0 else (wins.toDouble() / count) * 100.0

        return MistakeStat(
            name = mistake,
            count = count,
            totalPnlImpact = totalPnlImpact,
            avgPnlImpact = avgPnlImpact,
            winRate = winRate
        )
    }
}
