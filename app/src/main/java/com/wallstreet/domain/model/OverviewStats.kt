package com.wallstreet.domain.model

/**
 * High-level KPIs for the Analytics → Overview hero strip.
 * All figures are derived from closed trades (those with a realised P&L) in the
 * selected time period.
 */
data class OverviewStats(
    val netPnl: Double,
    val winRate: Double,
    val totalTrades: Int,
    /** Gross profit ÷ gross loss. Null when there are no losses (undefined / infinite). */
    val profitFactor: Double?
)
