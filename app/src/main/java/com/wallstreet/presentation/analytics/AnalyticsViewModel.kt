package com.wallstreet.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallstreet.domain.model.TimePeriod
import com.wallstreet.domain.repository.AuthRepository
import com.wallstreet.domain.usecase.analytics.GetCalendarDataUseCase
import com.wallstreet.domain.usecase.analytics.GetDayPerformanceUseCase
import com.wallstreet.domain.usecase.analytics.GetTradeSummaryUseCase
import com.wallstreet.domain.usecase.analytics.GetTrendPerformanceUseCase
import com.wallstreet.domain.usecase.home.getRecentTradeData
import com.wallstreet.domain.usecase.trade.GetTradesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyticsViewModel(
    private val authRepository: AuthRepository,
    private val getTradeSummaryUseCase: GetTradeSummaryUseCase,
    private val getDayPerformanceUseCase: GetDayPerformanceUseCase,
    private val getCalendarDataUseCase: GetCalendarDataUseCase,
    private val getTradesUseCase: GetTradesUseCase,
    private val getTrendPerformanceUseCase: GetTrendPerformanceUseCase
) : ViewModel() {

    private val _selectedTabIndex = MutableStateFlow(0)
    private val _selectedFilter = MutableStateFlow(TimePeriod.ONE_MONTH)
    private val _currentMonth = MutableStateFlow(YearMonth.now())

    val uiState: StateFlow<AnalyticsUiState> = _selectedFilter.flatMapLatest { filter ->
        val userId = authRepository.getCurrentUser()?.id ?: ""
        combine(
            getTradesUseCase(userId, filter, 500),
            _currentMonth,
            _selectedTabIndex
        ) { trades, currentMonth, tabIndex ->

            val summary = getTradeSummaryUseCase(trades)
            val performance = getDayPerformanceUseCase(trades)
            val calendarDays = getCalendarDataUseCase(currentMonth, trades)
            val trendPerformance = getTrendPerformanceUseCase(trades)

            AnalyticsUiState.Success(
                selectedFilter = filter,
                tradeSummary = summary,
                dayPerformance = performance,
                calendarDays = calendarDays,
                currentMonth = currentMonth,
                selectedTabIndex = tabIndex,
                recentTrades = getRecentTradeData(trades),
                trendPerformance = trendPerformance
            ) as AnalyticsUiState
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

    fun nextMonth() {
        _currentMonth.value = _currentMonth.value.plusMonths(1)
    }

    fun prevMonth() {
        _currentMonth.value = _currentMonth.value.minusMonths(1)
    }

    fun setMonth(month: YearMonth) {
        _currentMonth.value = month
    }

    override fun onCleared() {
        super.onCleared()
    }
}
