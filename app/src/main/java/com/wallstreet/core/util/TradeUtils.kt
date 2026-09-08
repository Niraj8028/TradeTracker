package com.wallstreet.core.util

import com.wallstreet.domain.model.Trade
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.Instant
import java.time.LocalDate
import java.util.Locale

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

/** Formats a raw dollar amount: 1380 → "1380", 23992 → "23992" etc. */
fun formatAmount(value: Double): String {
    val absValue = kotlin.math.abs(value).toInt()
    return absValue.toString()
}

/** Formats a PnL amount with prefix (+/-), currency symbol, and suffixes (K/M) for large values. */
fun Double.formatPnl(symbol: String = "$"): String {
    val value = this
    val prefix = if (value >= 0) "+" else "-"
    val abs = kotlin.math.abs(value)
    return when {
        abs >= 1_000_000 -> "$prefix$symbol${String.format(Locale.US, "%.2f", abs / 1_000_000)}M"
        abs >= 1_000 -> {
            val formatted = String.format(Locale.US, "%.0f", abs)
            val withCommas = buildString {
                formatted.reversed().forEachIndexed { i, c ->
                    if (i > 0 && i % 3 == 0) append(',')
                    append(c)
                }
            }.reversed()
            "$prefix$symbol$withCommas"
        }

        else -> "$prefix$symbol${String.format(Locale.US, "%.0f", abs)}"
    }
}

