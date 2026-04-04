package com.wallstreet.domain.usecase.strategy

import com.wallstreet.domain.model.Strategy
import com.wallstreet.domain.repository.StrategyRepository
import com.wallstreet.core.result.Result

class AddStrategyUseCase(private val strategyRepository: StrategyRepository) {

    suspend operator fun invoke(strategy: Strategy): Result<String> {
        if (strategy.name.isBlank()) {
            return Result.Error("Name can not be empty")
        }
        return strategyRepository.addStrategy(strategy)
    }
}