package com.wallstreet.core.util

fun Double.format(decimalPlaces: Int): String {
    return "%.${decimalPlaces}f".format(this)
}

fun Double.formatPercent(): String {
    return "${"%.2f".format(this)}%"
}