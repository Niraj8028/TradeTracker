package com.wallstreet.domain.model

data class SymbolStat(
    val symbol: String,
    val tradeCount: Int,
    val totalPnl: Double,
    val winRate: Double,
    val isBest: Boolean = false,
    val isWorst: Boolean = false
)
