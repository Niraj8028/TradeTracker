package com.wallstreet.presentation.analytics

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel


import java.time.LocalDate
import java.time.YearMonth

data class CalenderDay(
    val date: LocalDate,
//    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val isSelected: Boolean
)

class AnalyticsViewModel : ViewModel() {

    val gridSize = 42
    var selectedDate by mutableStateOf<LocalDate?>(LocalDate.now())
        private set
    var currentMonth by mutableStateOf(YearMonth.now())
        private set
    val calendarDays: MutableList<CalenderDay> = generateMonth(currentMonth)


    val days = listOf(
        "Sunday",
        "Monday",
        "Tuesday",
        "Wednesday",
        "Thursday",
        "Friday",
        "Saturday"
    )

    fun nextMoth() {
        currentMonth.plusMonths(1)
    }

    fun prevMoth() {
        currentMonth.minusMonths(1)

    }

    fun generateMonth(yearMonth: YearMonth): MutableList<CalenderDay> {

        val firstDay = yearMonth.atDay(1)
        val offset = firstDay.dayOfWeek.value % 7
        val daysInMonth = yearMonth.lengthOfMonth()
        val today = LocalDate.now()
        return MutableList(gridSize) { index ->

            when {
                index < offset -> {
                    val date = firstDay.minusDays((offset - index).toLong())
                    CalenderDay(date, date == today, false)
                }

                index < offset + daysInMonth -> {
                    val day = index - offset + 1
                    val date = yearMonth.atDay(day)
                    CalenderDay(date, date == today, false)
                }

                else -> {
                    val nextDay = index - (offset + daysInMonth) + 1
                    val date = yearMonth.plusMonths(1).atDay(nextDay)
                    CalenderDay(date, date == today, false)
                }
            }

        }
    }

}


