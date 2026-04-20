package com.wallstreet.data.model

import java.time.LocalDate

data class CalenderDay(
    val date: LocalDate,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val isSelected: Boolean
)