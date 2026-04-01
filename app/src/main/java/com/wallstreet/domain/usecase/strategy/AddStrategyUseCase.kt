package com.wallstreet.domain.usecase.strategy

import com.wallstreet.domain.model.UserStrategy
import com.wallstreet.domain.repository.UserStrategyRepository
import com.wallstreet.core.result.Result

class AddStrategyUseCase(private val userStrategyRepository: UserStrategyRepository) {

    suspend operator fun invoke(userStrategy: UserStrategy): Result<String> {
        if (userStrategy.name.isBlank()) {
            return Result.Error("Name can not be empty")
        }
        return userStrategyRepository.addStrategy(userStrategy)
    }
}