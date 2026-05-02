package com.wallstreet.domain.model

data class TradeStats(
    val count: Int,
    val pnl: Double,
    val winRate: Double,
    val percentage: Double
)
