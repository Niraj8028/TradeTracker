package com.wallstreet.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallstreet.domain.model.TimePeriod
import com.wallstreet.domain.repository.AuthRepository
import com.wallstreet.domain.usecase.analytics.GetDayPerformanceUseCase
import com.wallstreet.domain.usecase.analytics.GetOverviewStatsUseCase
import com.wallstreet.domain.usecase.analytics.GetTradeSummaryUseCase
import com.wallstreet.domain.usecase.analytics.GetTrendPerformanceUseCase
import com.wallstreet.core.perf.withTrace
import com.wallstreet.domain.usecase.home.GetMistakesAnalysisUsecase
import com.wallstreet.domain.usecase.home.getRecentTradeData
import com.wallstreet.domain.usecase.trade.GetTradesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyticsViewModel(
    private val authRepository: AuthRepository,
    private val getTradeSummaryUseCase: GetTradeSummaryUseCase,
    private val getDayPerformanceUseCase: GetDayPerformanceUseCase,
    private val getTradesUseCase: GetTradesUseCase,
    private val getTrendPerformanceUseCase: GetTrendPerformanceUseCase,
    private val getOverviewStatsUseCase: GetOverviewStatsUseCase,
    private val getMistakesAnalysisUsecase: GetMistakesAnalysisUsecase
) : ViewModel() {

    private val _selectedTabIndex = MutableStateFlow(0)
    private val _selectedFilter = MutableStateFlow(TimePeriod.ONE_MONTH)

    val uiState: StateFlow<AnalyticsUiState> = _selectedFilter.flatMapLatest { filter ->
        val userId = authRepository.getCurrentUser()?.id
            ?: return@flatMapLatest flowOf(AnalyticsUiState.Loading)
        combine(
            getTradesUseCase(userId, filter, 500),
            getTradesUseCase(userId, TimePeriod.ALL, 5000),
            _selectedTabIndex
        ) { trades, allTrades, tabIndex ->
            withTrace("analytics_compute") { trace ->
                trace.putAttribute("period", filter.name)
                trace.putAttribute("trade_count", trades.size.toString())

                val summary = getTradeSummaryUseCase(trades)
                val performance = getDayPerformanceUseCase(trades)
                val trendPerformance = getTrendPerformanceUseCase(trades)
                val overviewStats = getOverviewStatsUseCase(trades)
                val mistakesAnalysis = getMistakesAnalysisUsecase(trades)

                AnalyticsUiState.Success(
                    selectedFilter = filter,
                    tradeSummary = summary,
                    dayPerformance = performance,
                    allTrades = allTrades,
                    selectedTabIndex = tabIndex,
                    recentTrades = getRecentTradeData(trades),
                    trendPerformance = trendPerformance,
                    overviewStats = overviewStats,
                    mistakesAnalysis = mistakesAnalysis
                ) as AnalyticsUiState
            }
        }
    }.onStart {
        emit(AnalyticsUiState.Loading)
    }.catch { e ->
        emit(AnalyticsUiState.Error(e.message ?: "Unknown error"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyticsUiState.Loading
    )

    fun onTabSelect(index: Int) {
        _selectedTabIndex.value = index
    }

    fun onSelectFilter(filter: TimePeriod) {
        _selectedFilter.value = filter
    }
}
