package com.wallstreet.domain.usecase.trade

import com.wallstreet.domain.repository.TradeRepository

class CheckTodayTradeUseCase(
    private val repository: TradeRepository
) {
    suspend operator fun invoke(userId: String): Boolean {
        return repository.hasTradeToday(userId)
    }
}
