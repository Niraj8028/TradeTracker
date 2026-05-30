package com.wallstreet.domain.model

data class TrendStat(
    val direction: TrendDirection,
    val trades: Int,
    val totalPnl: Double,
    val winRate: Double
)

data class TrendPerformanceData(val stats: List<TrendStat>)
