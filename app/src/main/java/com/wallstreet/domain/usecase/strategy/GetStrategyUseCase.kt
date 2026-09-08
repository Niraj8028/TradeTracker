package com.wallstreet.domain.usecase.strategy

import com.wallstreet.domain.model.Strategy
import com.wallstreet.domain.repository.StrategyRepository
import kotlinx.coroutines.flow.Flow

class GetStrategyUseCase(private val strategyRepository: StrategyRepository) {

    suspend operator fun invoke(): Flow<List<Strategy>> {

        return strategyRepository.getStrategies()

    }
}