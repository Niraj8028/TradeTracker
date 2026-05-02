package com.wallstreet.domain.model

import java.time.DayOfWeek

data class DayStats(
    val day: DayOfWeek,
    val pnl: Double,
    val fraction: Float,
    val isProfit: Boolean
)
