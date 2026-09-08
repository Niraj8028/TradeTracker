package com.wallstreet.domain.model

data class TradeStats(
    val count: Int,
    val pnl: Double,
    val winRate: Double,
    val percentage: Double,
    /** Average realised P&L per closed trade in this direction. */
    val avgPnl: Double = 0.0
)
