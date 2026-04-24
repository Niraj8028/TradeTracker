package com.wallstreet.domain.model

data class RecentTradeItem(
    val id: String,
    val entryPrice: Double,
    val exitPrice: Double?,
    val quanity: Double,
    val tradeType: TradeType,
    val profitLoss: Double,
    val symbol: String,
    val tradeDate: Long
)
