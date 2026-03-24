package com.wallstreet.domain.usecase.trade

import com.wallstreet.core.result.Result
import com.wallstreet.domain.model.Trade
import com.wallstreet.domain.repository.TradeRepository
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

class GetTradesUsecase(private val tradeRepository: TradeRepository) {
     operator fun invoke(userId: String, limit: Int): Flow<List<Trade>> {
         Timber.d("GetTradesUsecase called userid $userId")
         val result = tradeRepository.getRecentTrades(userId, limit);
         Timber.d("GetTradesUsecase called userid ${result.toString()}")
        return result
    }
}