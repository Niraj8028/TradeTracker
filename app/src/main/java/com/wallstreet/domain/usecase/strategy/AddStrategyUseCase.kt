package com.wallstreet.domain.usecase.strategy

import com.wallstreet.domain.model.Strategy
import com.wallstreet.domain.repository.StrategyRepository
import com.wallstreet.core.result.Result

class AddStrategyUseCase(private val strategyRepository: StrategyRepository) {

    suspend operator fun invoke(strategy: Strategy): Result<String> {
        return strategyRepository.addStrategy(strategy)
    }
}