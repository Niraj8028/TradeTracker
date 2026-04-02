package com.wallstreet.domain.usecase.strategy

import com.wallstreet.domain.model.UserStrategy
import com.wallstreet.domain.repository.UserStrategyRepository
import kotlinx.coroutines.flow.Flow

class GetStrategyUseCase(private val userStrategyRepository: UserStrategyRepository) {

    suspend operator fun invoke(): Flow<List<UserStrategy>> {

        return userStrategyRepository.getStrategy()

    }
}