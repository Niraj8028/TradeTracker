package com.wallstreet.domain.model

import java.time.Period

enum class TimePeriod(val label: String) {
    ONE_WEEK("1W"),
    ONE_MONTH("1M"),
    THREE_MONTHS("3M"),
    SIX_MONTHS("6M"),
    ONE_YEAR("1Y"),
    ALL("All")
}

fun TimePeriod.toDuration(): Period = when (this) {
    TimePeriod.ONE_WEEK -> Period.ofDays(7)
    TimePeriod.ONE_MONTH -> Period.ofMonths(1)
    TimePeriod.THREE_MONTHS -> Period.ofMonths(3)
    TimePeriod.SIX_MONTHS -> Period.ofMonths(6)
    TimePeriod.ONE_YEAR -> Period.ofYears(1)
    TimePeriod.ALL -> Period.ofYears(100)
}
