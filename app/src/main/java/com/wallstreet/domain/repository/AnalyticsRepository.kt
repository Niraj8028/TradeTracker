package com.wallstreet.domain.repository

import com.wallstreet.domain.model.DayPerformance
import com.wallstreet.domain.model.TradeSummary
import com.wallstreet.domain.model.Trade
import com.wallstreet.domain.model.CalendarDay
import java.time.YearMonth

interface AnalyticsRepository {
    fun getTradeSummary(trades: List<Trade>): TradeSummary
    fun getDayPerformance(trades: List<Trade>): DayPerformance
    fun getCalendarData(yearMonth: YearMonth, trades: List<Trade>): List<CalendarDay>
}
