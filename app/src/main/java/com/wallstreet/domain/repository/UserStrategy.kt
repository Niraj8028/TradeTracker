package com.wallstreet.domain.repository

import com.wallstreet.core.result.Result
import kotlinx.coroutines.flow.Flow

interface UserStrategy {
    suspend fun addStrategy(userStrategy: UserStrategy): Result<String>
    suspend fun getStrategy(): Flow<List<UserStrategy>>
    suspend fun deleteStrategy(): Result<String>
    suspend fun updateStrategy(): Result<String>

}