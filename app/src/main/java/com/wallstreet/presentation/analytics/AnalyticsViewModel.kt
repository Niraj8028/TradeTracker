package com.wallstreet.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallstreet.data.store.TradeStore
import com.wallstreet.domain.model.Trade
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Instant


import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyticsViewModel(
    private val tradeStore: TradeStore
) : ViewModel() {

    private val _selectedTabIndex = MutableStateFlow(0)
    private val _selectedFilter = MutableStateFlow(TimePeriod.ONE_MONTH)
    private val _currentMonth = MutableStateFlow(YearMonth.now())

    val uiState: StateFlow<AnalyticsUiState> = _selectedFilter.flatMapLatest { filter ->
        val userId = authRepository.getCurrentUser()?.id ?: ""
        combine(
            getTradesUseCase(userId, filter, 500),
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

    private fun observeTrades() {
        viewModelScope.launch {
            tradeStore.trades.collect { tradeList ->
                trades = tradeList
                updateCalendar()
            }
        }
    }

    fun setMonth(month: YearMonth) {
        _currentMonth.value = month
    }

    fun generateMonth(yearMonth: YearMonth, trades: List<Trade>): List<CalenderDay> {

        val firstDay = yearMonth.atDay(1)
        val offset = firstDay.dayOfWeek.value % 7
        val daysInMonth = yearMonth.lengthOfMonth()
        val today = LocalDate.now()
        val tradesByDate = trades.groupBy {
            Instant.ofEpochMilli(it.tradeDate)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }

        return MutableList(gridSize) { index ->

            when {
                index < offset -> {
//                    val date = firstDay.minusDays((offset - index).toLong())//TODOS for future if next offests needed
                    CalenderDay(
                        date = null,
                        false,
                        false,
                        false
                    )//lets keep previouse calendar state to null for now
                }

                index < offset + daysInMonth -> {
                    val day = index - offset + 1
                    val date = yearMonth.atDay(day)
                    val dayTrades = tradesByDate[date] ?: emptyList()

                    val tradeCount = dayTrades.size
                    val pnl = dayTrades.sumOf {
                        it.profitLoss ?: it.calculateProfitLoss() ?: 0.0
                    }
                    Timber.d("Date: $date -> PnL: $pnl")

                    CalenderDay(
                        date = date,
                        isCurrentMonth = true,
                        isToday = date == today,
                        isSelected = false,
                        pnl = pnl,
                        tradeCount = tradeCount
                    )
                }

                else -> {
//                    val nextDay = index - (offset + daysInMonth) + 1 //TODOS for future if next offests needed
//                    val date = yearMonth.plusMonths(1).atDay(nextDay)
                    CalenderDay(
                        date = null,
                        false,
                        false,
                        false,

                        )//lets keep next calendar state to null for now
                }
            }

        }
    }

}
