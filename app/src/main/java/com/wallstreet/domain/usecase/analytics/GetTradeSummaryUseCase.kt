package com.wallstreet.domain.usecase.analytics

import com.wallstreet.domain.model.TradeSummary
import com.wallstreet.domain.model.Trade
import com.wallstreet.domain.repository.AnalyticsRepository

class GetTradeSummaryUseCase(private val repository: AnalyticsRepository) {
    operator fun invoke(trades: List<Trade>): TradeSummary {
        return repository.getTradeSummary(trades)
    }
}
