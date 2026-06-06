package com.wallstreet.domain.usecase.analytics

import com.wallstreet.domain.model.OverviewStats
import com.wallstreet.domain.model.Trade
import com.wallstreet.domain.repository.AnalyticsRepository

class GetOverviewStatsUseCase(private val repository: AnalyticsRepository) {
    operator fun invoke(trades: List<Trade>): OverviewStats {
        return repository.getOverviewStats(trades)
    }
}
