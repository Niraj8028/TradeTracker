package com.wallstreet.domain.usecase.trade

import com.wallstreet.domain.model.TimePeriod
import com.wallstreet.domain.model.Trade
import com.wallstreet.domain.model.toDuration
import com.wallstreet.domain.repository.TradeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate
import java.time.ZoneId

class GetTradesUseCase(private val tradeRepository: TradeRepository) {
    operator fun invoke(userId: String, period: TimePeriod, limit: Int): Flow<List<Trade>> {
        if (userId.isBlank()) return flowOf(emptyList())
        if (period == TimePeriod.ALL) {
            return tradeRepository.getAllTrades(userId)
        }
        val cutOfMillis = LocalDate.now()
            .minus(period.toDuration())
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        return tradeRepository.getRecentTrades(userId, cutOfMillis, limit)
    }
}


