package com.wallstreet.domain.model.strategy

import com.wallstreet.domain.model.Strategy
import com.wallstreet.domain.model.TimePeriod

data class StrategyStats(
    val strategy: Strategy,
    val totalTrades: Int,
    val totalPnl: Double,
    val winRate: Double,
    val rrRatio: Double,        // Avg Win / Avg Loss
    val avgProfitPerTrade: Double,
    val period: TimePeriod,
    val sparkline: List<Double> = emptyList()   // cumulative P&L points, chronological
)