package com.wallstreet.domain.usecase.trade

import com.wallstreet.core.result.Result
import com.wallstreet.domain.model.Trade
import com.wallstreet.domain.repository.TradeRepository
import kotlinx.coroutines.flow.Flow

class GetTradesUsecase(private val tradeRepository: TradeRepository) {
     operator fun invoke(userId: String, limit: Int): Flow<List<Trade>> {
        return tradeRepository.getRecentTrades(userId, limit);
    }
}