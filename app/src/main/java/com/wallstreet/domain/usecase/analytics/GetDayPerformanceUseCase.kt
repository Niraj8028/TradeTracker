package com.wallstreet.domain.usecase.analytics

import com.wallstreet.domain.model.DayPerformance
import com.wallstreet.domain.model.Trade
import com.wallstreet.domain.repository.AnalyticsRepository

class GetDayPerformanceUseCase(private val repository: AnalyticsRepository) {
    operator fun invoke(trades: List<Trade>): DayPerformance {
        return repository.getDayPerformance(trades)
    }
}
