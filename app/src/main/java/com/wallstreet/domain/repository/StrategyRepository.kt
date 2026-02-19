package com.wallstreet.domain.repository

import com.wallstreet.domain.model.Strategy
import kotlinx.coroutines.flow.Flow

interface StrategyRepository {
    fun getStrategies(): Flow<List<Strategy>>
    suspend fun getStrategyById(id: String): Strategy?
    suspend fun saveStrategy(strategy: Strategy)
    suspend fun deleteStrategy(id: String)
}