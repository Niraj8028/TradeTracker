package com.wallstreet.domain.usecase.strategy

import com.wallstreet.domain.model.UserStrategy
import com.wallstreet.domain.repository.UserStrategyRepository
import com.wallstreet.core.result.Result

class UpdateStrategyUseCase(private val userStrategyRepository: UserStrategyRepository) {

    suspend operator fun invoke(userStrategy: UserStrategy): Result<String> {
        if (userStrategy.id.isEmpty()) {
            return Result.Error("Id can not be empty")

        }

        return userStrategyRepository.updateStrategy(userStrategy)
    }
}