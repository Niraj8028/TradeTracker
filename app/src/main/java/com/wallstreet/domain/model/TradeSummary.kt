package com.wallstreet.domain.model

data class TradeSummary(
    val long: TradeStats,
    val short: TradeStats,
    val totalTrades: Int
)
