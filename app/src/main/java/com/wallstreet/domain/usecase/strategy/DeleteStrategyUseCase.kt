package com.wallstreet.domain.usecase.strategy

import com.wallstreet.core.result.Result
import com.wallstreet.domain.model.Strategy
import com.wallstreet.domain.repository.StrategyRepository

class DeleteStrategyUseCase(private val strategyRepository: StrategyRepository) {

    suspend operator fun invoke(strategies: List<Strategy>): Result<String> {
        if (strategies.isEmpty()) return Result.Error("No strategies selected")
        strategies.forEach { strategy ->
            val result = strategyRepository.deleteStrategy(strategy)
            if (result is Result.Error) return result
        }
        return Result.Success("Deleted successfully")
    }
}