package com.wallstreet.domain.usecase.analytics

import com.wallstreet.domain.model.CalendarDay
import com.wallstreet.domain.model.Trade
import com.wallstreet.domain.repository.AnalyticsRepository
import java.time.YearMonth

class GetCalendarDataUseCase(private val repository: AnalyticsRepository) {
    operator fun invoke(yearMonth: YearMonth, trades: List<Trade>): List<CalendarDay> {
        return repository.getCalendarData(yearMonth, trades)
    }
}
