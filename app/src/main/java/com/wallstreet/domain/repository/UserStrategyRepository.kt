package com.wallstreet.domain.repository

import com.wallstreet.core.result.Result
import com.wallstreet.domain.model.UserStrategy
import kotlinx.coroutines.flow.Flow

interface UserStrategyRepository {
    suspend fun addStrategy(userStrategy: UserStrategy): Result<String>
    suspend fun getStrategy(): Flow<List<UserStrategy>>
    suspend fun deleteStrategy(userStrategy: UserStrategy): Result<String>
    suspend fun updateStrategy(userStrategy: UserStrategy): Result<String>

}