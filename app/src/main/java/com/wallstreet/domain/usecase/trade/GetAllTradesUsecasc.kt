package com.wallstreet.domain.usecase.trade

import com.wallstreet.domain.model.Trade
import com.wallstreet.domain.repository.TradeRepository
import kotlinx.coroutines.flow.Flow

class GetAllTradesUsecasc(private val tradeRepository: TradeRepository) {
    operator fun invoke(userId: String): Flow<List<Trade>> {
        return tradeRepository.getAllTrades(userId);
    }
}