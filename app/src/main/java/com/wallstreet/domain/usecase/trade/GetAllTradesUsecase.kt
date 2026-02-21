package com.wallstreet.domain.usecase.trade

import com.wallstreet.core.result.Result
import com.wallstreet.domain.model.Trade
import com.wallstreet.domain.model.User
import com.wallstreet.domain.repository.TradeRepository

class GetAllTradesUsecase(private val tradeRepository: TradeRepository) {
    suspend operator fun invoke(userId: String): Result<List<Trade>> {
        return tradeRepository.getAllTrades(userId);
    }
}