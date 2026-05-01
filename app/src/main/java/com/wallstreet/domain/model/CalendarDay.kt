package com.wallstreet.domain.model

import java.time.LocalDate

data class CalendarDay(
    val date: LocalDate?,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val isSelected: Boolean,
    val pnl: Double = 0.0,
    val tradeCount: Int = 0
)
