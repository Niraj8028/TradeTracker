package com.wallstreet.domain.model

data class Strategy(
    val id: String,
    val userId: String,
    val name: String,
    val description: String,
    val winRate: Double,
    val profitFactor: Double,
    val netPnl: Double,
    val expectedValue: Double,
    val tradeIds: List<String> = emptyList(),
    val synced: Boolean = false
)