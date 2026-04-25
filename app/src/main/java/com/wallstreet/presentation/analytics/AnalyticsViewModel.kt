package com.wallstreet.presentation.analytics

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallstreet.data.store.TradeStore
import com.wallstreet.domain.model.Trade
import com.wallstreet.domain.repository.AuthRepository
import com.wallstreet.presentation.analytics.components.FilterOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Instant


import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import kotlin.collections.emptyList

data class CalenderDay(
    val date: LocalDate?,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val isSelected: Boolean,
    val pnl: Double = 0.0,
    val tradeCount: Int = 0
)

class AnalyticsViewModel(
    private val authRepository: AuthRepository,
    private val tradeStore: TradeStore

) : ViewModel() {

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex = _selectedTabIndex.asStateFlow()
    private val _selectedFilter = MutableStateFlow<FilterOption>(FilterOption.OneWeek)

    val selectedFilter = _selectedFilter.asStateFlow()


    fun onTabSelect(index: Int) {
        _selectedTabIndex.value = index
    }

    fun onSelectFilter(filter: FilterOption) {
        _selectedFilter.value = filter
    }

    val gridSize = 42

    //    var selectedDate by mutableStateOf<LocalDate?>(LocalDate.now())
//        private set
    var currentMonth by mutableStateOf(YearMonth.now())
        private set
    var calendarDays by mutableStateOf<List<CalenderDay>>(emptyList())
        private set
    var trades by mutableStateOf<List<Trade>>(emptyList())
        private set

    val days = listOf(
        "SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"
    )


    fun nextMoth() {
        currentMonth = currentMonth.plusMonths(1)
        updateCalendar()
    }

    fun prevMoth() {
        currentMonth = currentMonth.minusMonths(1)
        updateCalendar()
    }

    fun setMonth(month: YearMonth) {
        currentMonth = month
        updateCalendar()
    }


    private fun updateCalendar() {
        calendarDays = generateMonth(currentMonth, trades)
    }

    private fun observeTrades() {
        val userId = authRepository.getCurrentUser()?.id ?: return

        tradeStore.startObserving(userId)

        viewModelScope.launch {
            tradeStore.trades.collect { tradeList ->
                trades = tradeList
                updateCalendar()
            }
        }
    }

    init {
        observeTrades()
    }

    override fun onCleared() {
        super.onCleared()
        tradeStore.stopObserving()
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


