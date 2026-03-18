package com.wallstreet.domain.model

data class HomeStats(
    val totalPnl: Double,
    val totalTrades: Int,
    val winRate: Double,
    val totalWinningTrades: Int,
    val totalLosingTrades: Int,
    val avgProfit: Double,
    val avgLoss: Double,
    val riskRewardRatio: Double,
    val profitPercentage: Double
    )
