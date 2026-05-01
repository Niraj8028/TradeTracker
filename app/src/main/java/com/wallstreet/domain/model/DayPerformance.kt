package com.wallstreet.domain.model

import java.time.DayOfWeek

data class DayPerformance(
    val days: List<DayStats>,
    val bestDay: DayOfWeek?
)
