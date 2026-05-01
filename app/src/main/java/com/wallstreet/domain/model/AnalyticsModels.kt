package com.wallstreet.domain.model

import java.time.DayOfWeek

data class TradeStats(
    val count: Int,
    val pnl: Double,
    val winRate: Double,
    val percentage: Double
)

data class TradeSummary(
    val long: TradeStats,
    val short: TradeStats,
    val totalTrades: Int
)

data class DayStats(val day: DayOfWeek, val pnl: Double, val fraction: Float, val isProfit: Boolean)
data class DayPerformance(val days: List<DayStats>, val bestDay: DayOfWeek?)
