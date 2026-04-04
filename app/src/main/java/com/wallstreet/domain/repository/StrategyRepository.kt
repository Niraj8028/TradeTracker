package com.wallstreet.domain.repository

import com.wallstreet.core.result.Result
import com.wallstreet.domain.model.Strategy
import kotlinx.coroutines.flow.Flow

interface StrategyRepository {
    suspend fun addStrategy(strategy: Strategy): Result<String>
    suspend fun getStrategy(): Flow<List<Strategy>>
    suspend fun deleteStrategy(strategy: Strategy): Result<String>
    suspend fun updateStrategy(strategy: Strategy): Result<String>

}