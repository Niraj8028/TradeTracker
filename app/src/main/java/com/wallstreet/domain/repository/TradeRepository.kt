package com.wallstreet.domain.repository

import com.wallstreet.core.result.Result
import com.wallstreet.domain.model.Trade
import kotlinx.coroutines.flow.Flow

interface TradeRepository {
    suspend fun getAllTrades(userId: String): Result<List<Trade>>
    suspend fun addTrade(trade: Trade): Result<String>
    suspend fun getRecentTrades(userId: String, limit: Int = 10): Result<List<Trade>>
}