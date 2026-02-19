package com.wallstreet.core.util

import kotlin.math.abs

object CurrencyUtils {
    fun formatPnl(amount: Double, symbol: String = "$"): String {
        val formatted = String.format("%.2f", abs(amount))
        return if (amount >= 0) "+$symbol$formatted" else "-$symbol$formatted"
    }

    fun formatPercent(value: Double): String {
        val formatted = String.format("%.1f", abs(value))
        return if (value >= 0) "+$formatted%" else "-$formatted%"
    }
}