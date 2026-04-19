package com.wallstreet.presentation.analytics

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallstreet.domain.model.Trade
import com.wallstreet.domain.repository.AuthRepository
import com.wallstreet.domain.usecase.trade.GetAllTradesUsecase
import com.wallstreet.domain.usecase.trade.GetTradesUsecase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.launch
import timber.log.Timber


import java.time.LocalDate
import java.time.YearMonth
import kotlin.collections.emptyList

data class CalenderDay(
    val date: LocalDate,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val isSelected: Boolean
)

class AnalyticsViewModel(
    private val getAllTradesUsecase: GetAllTradesUsecase,
    private val authRepository: AuthRepository
) : ViewModel() {

    val gridSize = 42
    var selectedDate by mutableStateOf<LocalDate?>(LocalDate.now())
        private set
    var currentMonth by mutableStateOf(YearMonth.now())
        private set
    val calendarDays: List<CalenderDay> = generateMonth(currentMonth)


    val days = listOf(
        "SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"
    )


    fun nextMoth() {
        currentMonth = currentMonth.plusMonths(1)
    }

    fun prevMoth() {
        currentMonth = currentMonth.minusMonths(1)

    }

    suspend fun allTrades() {
        val userId = authRepository.getCurrentUser()?.id.toString()

        if (userId?.isEmpty() == true) return


        viewModelScope.launch { }
        getAllTradesUsecase(
            userId
        ).collect { trades ->
            trades.forEach {
                Timber.d("Trade: $it")
            }
        }

    }

    fun generateMonth(yearMonth: YearMonth): List<CalenderDay> {

        val firstDay = yearMonth.atDay(1)
        val offset = firstDay.dayOfWeek.value % 7
        val daysInMonth = yearMonth.lengthOfMonth()
        val today = LocalDate.now()
        return MutableList(gridSize) { index ->

            when {
                index < offset -> {
                    val date = firstDay.minusDays((offset - index).toLong())
                    CalenderDay(date, false, date == today, false)
                }

                index < offset + daysInMonth -> {
                    val day = index - offset + 1
                    val date = yearMonth.atDay(day)
                    CalenderDay(date, true, date == today, false)
                }

                else -> {
                    val nextDay = index - (offset + daysInMonth) + 1
                    val date = yearMonth.plusMonths(1).atDay(nextDay)
                    CalenderDay(date, false, date == today, false)
                }
            }

        }
    }

}


