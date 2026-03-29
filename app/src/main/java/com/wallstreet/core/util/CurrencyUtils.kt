package com.wallstreet.core.util

fun Double.format(decimalPlaces: Int): String {
    return "%.${decimalPlaces}f".format(this)
}

fun Double.formatPnl(): String {
    val sign = if (this > 0) '+' else '-';
    return "$sign$${"%.2f".format(this)}"
}

fun Double.formatPercent(): String {
    return "${"%.2f".format(this)}%"
}