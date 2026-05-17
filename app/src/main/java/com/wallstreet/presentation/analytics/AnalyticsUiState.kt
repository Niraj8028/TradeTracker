package com.wallstreet.presentation.analytics

import com.wallstreet.domain.model.DayPerformance
import com.wallstreet.domain.model.TradeSummary
import com.wallstreet.domain.model.CalendarDay
import com.wallstreet.domain.model.RecentTradeItem
import com.wallstreet.domain.model.TimePeriod
import com.wallstreet.domain.model.TrendPerformanceData
import java.time.YearMonth

sealed class AnalyticsUiState {
    data object Loading : AnalyticsUiState()
    data class Error(val error: String) : AnalyticsUiState()
    data class Success(
        val selectedFilter: TimePeriod,
        val tradeSummary: TradeSummary,
        val dayPerformance: DayPerformance,
        val calendarDays: List<CalendarDay>,
        val currentMonth: YearMonth,
        val selectedTabIndex: Int,
        val recentTrades: List<RecentTradeItem>,
        val trendPerformance: TrendPerformanceData
    ) : AnalyticsUiState()
}
