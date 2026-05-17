package com.wallstreet.domain.usecase.analytics

import com.wallstreet.domain.model.Trade
import com.wallstreet.domain.model.TrendPerformanceData
import com.wallstreet.domain.repository.AnalyticsRepository

class GetTrendPerformanceUseCase(private val repository: AnalyticsRepository) {
    operator fun invoke(trades: List<Trade>): TrendPerformanceData {
        return repository.getTrendPerformance(trades)
    }
}
