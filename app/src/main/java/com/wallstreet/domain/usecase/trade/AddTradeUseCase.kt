package com.wallstreet.domain.usecase.trade

import com.wallstreet.core.result.Result
import com.wallstreet.domain.model.Trade
import com.wallstreet.domain.repository.TradeRepository

class AddTradeUseCase( private val tradeRepository: TradeRepository) {

    // TODO Add trade validations
    suspend operator fun invoke(trade: Trade): Result<String> {
        if (trade.symbol.isBlank()) {
            return Result.Error("Symbol cannot be empty")
        }
        if (trade.quantity <= 0) {
            return Result.Error("Quantity must be positive")
        }

        return tradeRepository.addTrade(trade)
    }
}