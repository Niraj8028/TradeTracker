package com.wallstreet.domain.model

import java.time.LocalDate

data class DailySummary(
    val date: LocalDate,
    val totalPnl: Double,
    val tradesCount: Int,
    val symbols: List<String>
)