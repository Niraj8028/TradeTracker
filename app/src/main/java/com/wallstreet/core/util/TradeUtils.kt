package com.wallstreet.core.util

import com.wallstreet.domain.model.Trade
import com.wallstreet.domain.model.TradeType
import java.time.DayOfWeek
import kotlin.math.abs
import java.time.ZoneId
import java.time.Instant

data class TradeStats(
    val count: Int,
    val pnl: Double,
    val winRate: Double,
    val percentage: Double
)

data class TradeSummary(
    val long: TradeStats,
    val short: TradeStats,
    val totalTrades: Int
)

data class RawStats(
    val count: Int,
    val pnl: Double,
    val wins: Int
)

data class DayStats(val day: DayOfWeek, val pnl: Double, val fraction: Float, val isProfit: Boolean)
data class DayPerformance(val days: List<DayStats>, val bestDay: DayOfWeek?)

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
        short = shortStats,
        totalTrades = trades.size

    )
}


fun getDayPerformance(trades: List<Trade>): DayPerformance {

    // get aggregate pnl by day one pass
    val pnlByDay: Map<DayOfWeek, Double> = trades
        .groupBy { it.toDayOfWeek() }
        .mapValues { (_, list) ->
            list.sumOf { it.profitLoss ?: 0.0 }
        }
    val orderedDays = listOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY,
        DayOfWeek.SUNDAY
    )
    val normalized: List<Pair<DayOfWeek, Double>> =
        orderedDays.map { day ->
            day to (pnlByDay[day] ?: 0.0)
        }

    //  Find max abs pnl for scaling
    val maxAbs = normalized.maxOfOrNull { abs(it.second) } ?: 0.0

    //   Build UI model
    val dayStats = normalized.map { (day, pnl) ->
        val fraction = if (maxAbs == 0.0) 0f else (pnl / maxAbs).toFloat()
        DayStats(
            day = day,
            pnl = pnl,
            fraction = fraction,     // use for bar height
            isProfit = pnl > 0
        )
    }

    //  est day
    val best = normalized.maxByOrNull { it.second }?.first

    return DayPerformance(
        days = dayStats,
        bestDay = best
    )
}

private fun Trade.toDayOfWeek(): DayOfWeek {
    return Instant.ofEpochMilli(this.tradeDate)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .dayOfWeek
}