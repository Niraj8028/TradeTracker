package com.wallstreet.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallstreet.data.store.TradeStore
import com.wallstreet.domain.model.TimePeriod
import com.wallstreet.domain.repository.AuthRepository
import com.wallstreet.domain.usecase.analytics.GetCalendarDataUseCase
import com.wallstreet.domain.usecase.analytics.GetDayPerformanceUseCase
import com.wallstreet.domain.usecase.analytics.GetTradeSummaryUseCase
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
    private val tradeStore: TradeStore,
    private val getTradeSummaryUseCase: GetTradeSummaryUseCase,
    private val getDayPerformanceUseCase: GetDayPerformanceUseCase,
    private val getCalendarDataUseCase: GetCalendarDataUseCase,
    private val getTradesUsecase: GetTradesUseCase
) : ViewModel() {

    private val _selectedTabIndex = MutableStateFlow(0)
    private val _selectedFilter = MutableStateFlow(TimePeriod.ONE_MONTH)
    private val _currentMonth = MutableStateFlow(YearMonth.now())

    val uiState: StateFlow<AnalyticsUiState> = _selectedFilter.flatMapLatest { filter ->
        val userId = authRepository.getCurrentUser()?.id ?: ""
        combine(
            getTradesUsecase(userId, filter, 500),
            tradeStore.trades,
            _currentMonth,
            _selectedTabIndex
        ) { filteredTrades, allTrades, month, tabIndex ->

            val summary = getTradeSummaryUseCase(filteredTrades)
            val performance = getDayPerformanceUseCase(filteredTrades)
            val calendarDays = getCalendarDataUseCase(month, allTrades)

            AnalyticsUiState.Success(
                selectedFilter = filter,
                tradeSummary = summary,
                dayPerformance = performance,
                calendarDays = calendarDays,
                currentMonth = month,
                selectedTabIndex = tabIndex,
                recentTrades = getRecentTradeData(filteredTrades)
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

    init {
        val userId = authRepository.getCurrentUser()?.id
        if (userId != null) {
            tradeStore.startObserving(userId)
        }
    }

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
        tradeStore.stopObserving()
    }
}
