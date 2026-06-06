package com.wallstreet.presentation.analytics

import com.wallstreet.domain.model.DayPerformance
import com.wallstreet.domain.model.MistakesAnalysisData
import com.wallstreet.domain.model.OverviewStats
import com.wallstreet.domain.model.TradeSummary
import com.wallstreet.domain.model.RecentTradeItem
import com.wallstreet.domain.model.TimePeriod
import com.wallstreet.domain.model.Trade
import com.wallstreet.domain.model.TrendPerformanceData

sealed class AnalyticsUiState {
    data object Loading : AnalyticsUiState()
    data class Error(val error: String) : AnalyticsUiState()
    data class Success(
        val selectedFilter: TimePeriod,
        val tradeSummary: TradeSummary,
        val dayPerformance: DayPerformance,
        val allTrades: List<Trade>,
        val selectedTabIndex: Int,
        val recentTrades: List<RecentTradeItem>,
        val trendPerformance: TrendPerformanceData,
        val overviewStats: OverviewStats,
        val mistakesAnalysis: MistakesAnalysisData
    ) : AnalyticsUiState()
}
