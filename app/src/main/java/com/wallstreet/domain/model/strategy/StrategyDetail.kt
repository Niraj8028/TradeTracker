package com.wallstreet.domain.model.strategy

import com.wallstreet.domain.model.Strategy
import com.wallstreet.presentation.home.TimePeriod


data class StrategyDetail(
    val strategy: Strategy,
    val totalTrades: Int,
    val totalPnl: Double,
    val winRate: Double,
    val rrRatio: Double,
    val avgProfitPerTrade: Double,
    val maxProfit: Double,
    val maxLoss: Double,
    val bestSymbol: String?,
    val longStats: TradeTypeStats,
    val shortStats: TradeTypeStats,
    val period: TimePeriod
)

data class TradeTypeStats(
    val totalTrades: Int,
    val winRate: Double,
    val totalPnl: Double,
    val avgWin: Double,
    val rrRatio: Double
)