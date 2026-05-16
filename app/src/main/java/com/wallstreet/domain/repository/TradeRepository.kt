package com.wallstreet.domain.repository

import com.wallstreet.core.result.Result
import com.wallstreet.domain.model.Trade
import kotlinx.coroutines.flow.Flow

interface TradeRepository {
    fun getAllTrades(userId: String): Flow<List<Trade>>
    suspend fun addTrade(trade: Trade): Result<String>
    fun getRecentTrades(userId: String, fromMilis: Long, limit: Int): Flow<List<Trade>>
    suspend fun syncPendingTrades(userId: String): Boolean
    suspend fun clearLocalData(userId: String)
    suspend fun hasPendingTrades(userId: String): Boolean
    suspend fun seedFromFirestore(userId: String)
}