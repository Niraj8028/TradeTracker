package com.wallstreet.domain.model

import com.wallstreet.presentation.home.TimePeriod

data class HomeStats(
    val totalPnl: Double,
    val totalTrades: Int,
    val winRate: Double,
    val totalWinningTrades: Int,
    val totalLosingTrades: Int,
    val avgProfit: Double,
    val avgLoss: Double,
    val riskRewardRatio: Double,
    val profitPercentage: Double,
    val selectedPeriod: TimePeriod = TimePeriod.ONE_MONTH
    )
