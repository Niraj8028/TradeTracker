package com.wallstreet.core.util

import com.wallstreet.domain.model.Trade
import com.wallstreet.domain.model.TradeType


data class TradeStats(
    val count: Int,
    val pnl: Double,
    val winRate: Double,
    val percentage: Double
)

data class TradeSummary(
    val long: TradeStats,
    val short: TradeStats
)

data class RawStats(
    val count: Int,
    val pnl: Double,
    val wins: Int
)

//splitting the trade
fun splitTrades(trades: List<Trade>): Pair<List<Trade>, List<Trade>> {
    val long = mutableListOf<Trade>()
    val short = mutableListOf<Trade>()

    trades.forEach {
        if (it.tradeType == TradeType.LONG) long.add(it)
        else short.add(it)
    }

    return long to short
}

//
fun aggregate(trades: List<Trade>): RawStats {
    var pnl = 0.0
    var wins = 0

    trades.forEach {
        val profit = it.profitLoss ?: 0.0
        pnl += profit
        if (profit > 0) wins++
    }

    return RawStats(
        count = trades.size,
        pnl = pnl,
        wins = wins
    )
}

//formulas
fun calculateWinRate(wins: Int, count: Int): Double {
    return if (count == 0) 0.0 else (wins * 100.0) / count
}

fun calculatePercentage(count: Int, total: Int): Double {
    return if (total == 0) 0.0 else (count * 100.0) / total
}


fun buildStats(raw: RawStats, total: Int): TradeStats {
    return TradeStats(
        count = raw.count,
        pnl = raw.pnl,
        winRate = calculateWinRate(raw.wins, raw.count),
        percentage = calculatePercentage(raw.count, total)
    )
}
//getting required summary data for ui

fun getTradeSummary(trades: List<Trade>): TradeSummary {

    val (longTrades, shortTrades) = splitTrades(trades)

    val total = trades.size

    val longRaw = aggregate(longTrades)
    val shortRaw = aggregate(shortTrades)

    val longStats = buildStats(longRaw, total)
    val shortStats = buildStats(shortRaw, total)

    return TradeSummary(
        long = longStats,
        short = shortStats
    )
}