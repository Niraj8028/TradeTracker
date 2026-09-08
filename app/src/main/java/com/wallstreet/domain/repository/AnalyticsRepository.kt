package com.wallstreet.domain.repository

import com.wallstreet.domain.model.DayPerformance
import com.wallstreet.domain.model.OverviewStats
import com.wallstreet.domain.model.TradeSummary
import com.wallstreet.domain.model.Trade
import com.wallstreet.domain.model.CalendarDay
import com.wallstreet.domain.model.TrendPerformanceData
import java.time.YearMonth

interface AnalyticsRepository {
    fun getTradeSummary(trades: List<Trade>): TradeSummary
    fun getOverviewStats(trades: List<Trade>): OverviewStats
    fun getDayPerformance(trades: List<Trade>): DayPerformance
    fun getCalendarData(yearMonth: YearMonth, trades: List<Trade>): List<CalendarDay>
    fun getTrendPerformance(trades: List<Trade>): TrendPerformanceData
}
