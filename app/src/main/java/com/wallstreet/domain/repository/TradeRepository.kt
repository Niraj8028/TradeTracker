package com.wallstreet.domain.repository

import com.wallstreet.core.result.Result
import com.wallstreet.domain.model.Trade
import kotlinx.coroutines.flow.Flow

interface TradeRepository {
    suspend fun getAllTrades(userId: String): Result<List<Trade>>
    suspend fun getTradeById(tradeId: String): Result<Trade>
    suspend fun addTrade(trade: Trade): Result<String>
    suspend fun updateTrade(trade: Trade): Result<String>
    suspend fun deleteTrade(tradeId: String): Result<String>
    fun observeTrades(userId: String): Flow<List<Trade>>
}