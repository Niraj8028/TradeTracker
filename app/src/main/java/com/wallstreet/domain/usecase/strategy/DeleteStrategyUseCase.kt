package com.wallstreet.domain.usecase.strategy

import com.wallstreet.core.result.Result
import com.wallstreet.domain.model.Strategy
import com.wallstreet.domain.repository.StrategyRepository

class DeleteStrategyUseCase(private val strategyRepository: StrategyRepository) {

    suspend operator fun invoke(strategy: Strategy): Result<String> {

        return strategyRepository.deleteStrategy(strategy)
    }


}