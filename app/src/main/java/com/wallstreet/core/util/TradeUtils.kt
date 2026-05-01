package com.wallstreet.core.util

import com.wallstreet.domain.model.Trade
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.Instant
import java.time.LocalDate

fun List<Trade>.calculateTotalPnL(): Double =
    sumOf { it.profitLoss ?: it.calculateProfitLoss() ?: 0.0 }

fun List<Trade>.calculateWinRate(): Double {
    if (isEmpty()) return 0.0
    val wins = count { (it.profitLoss ?: it.calculateProfitLoss() ?: 0.0) > 0 }
    return (wins.toDouble() / size) * 100
}

fun Trade.toLocalDate(): LocalDate {
    return Instant.ofEpochMilli(this.tradeDate)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}

fun Trade.toDayOfWeek(): DayOfWeek = toLocalDate().dayOfWeek
