package com.wallstreet.domain.usecase.trade

import com.wallstreet.core.result.Result
import com.wallstreet.domain.model.Trade
import com.wallstreet.domain.repository.TradeRepository

class GetRecentTradesUsecase(private val tradeRepository: TradeRepository) {
    suspend operator fun invoke(userId: String, limit: Int): Result<List<Trade>> {
        return tradeRepository.getRecentTrades(userId, limit);
    }
}