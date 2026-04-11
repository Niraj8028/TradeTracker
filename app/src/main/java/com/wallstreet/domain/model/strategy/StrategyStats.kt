package com.wallstreet.domain.model.strategy

import com.wallstreet.domain.model.Strategy
import com.wallstreet.presentation.home.TimePeriod

data class StrategyStats(
    val strategy: Strategy,
    val totalTrades: Int,
    val totalPnl: Double,
    val winRate: Double,
    val rrRatio: Double,        // Avg Win / Avg Loss
    val avgProfitPerTrade: Double,
    val period: TimePeriod
)