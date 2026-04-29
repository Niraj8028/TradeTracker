package com.wallstreet.domain.usecase.home

import com.wallstreet.domain.model.HomeStats
import com.wallstreet.domain.model.Trade

class GetHomeStateUsecase {
    operator fun invoke(trades: List<Trade>): HomeStats {
        return computeHomeDate(trades)
    }

    private fun computeHomeDate(trades: List<Trade>): HomeStats {
        if(trades.isEmpty()) {
            return HomeStats(
                totalPnl = 0.0,
                totalTrades = 0,
                winRate = 0.0,
                totalWinningTrades = 0,
                totalLosingTrades = 0,
                avgProfit = 0.0,
                avgLoss = 0.0,
                riskRewardRatio = 0.0,
                profitPercentage = 0.0
            )
        }
        val totalPnl = trades.sumOf { it.profitLoss ?: 0.0 };
        val totalTrades = trades.size;
        val winningTrades = trades.filter { (it.profitLoss ?: 0.0) >=0.0 }
        val losingTrades = trades.filter { (it.profitLoss ?: 0.0) < 0.0 }
        val totalWinningTrades = winningTrades.size
        val totalLosingTrades = losingTrades.size
        val winRate: Double = (totalWinningTrades.toDouble() / (totalWinningTrades + totalLosingTrades)) * 100
        val avgProfit = if(totalWinningTrades == 0) 0.0 else winningTrades.sumOf { it.profitLoss ?: 0.0 } / totalWinningTrades
        val avgLoss = if(totalLosingTrades == 0) 0.0 else losingTrades.sumOf { it.profitLoss ?: 0.0 } / totalLosingTrades
        val riskRewardRatio =
            if (avgLoss != 0.0) avgProfit / kotlin.math.abs(avgLoss) else 0.0


        return HomeStats(
            totalPnl = totalPnl,
            totalTrades = totalTrades,
            winRate = winRate,
            totalWinningTrades = totalWinningTrades,
            totalLosingTrades = totalLosingTrades,
            avgProfit = avgProfit,
            avgLoss = avgLoss,
            riskRewardRatio = riskRewardRatio,
            profitPercentage = winRate,
        )

    }
}