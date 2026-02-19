package com.wallstreet.domain.repository

import com.wallstreet.domain.model.AssetType
import com.wallstreet.domain.model.Trade
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface TradeRepository {
    fun getTradeHistory(
        assetType: AssetType? = null,
        from: LocalDate? = null,
        to: LocalDate? = null
    ): Flow<List<Trade>>

    fun getRecentTrades(limit: Int = 10): Flow<List<Trade>>
    suspend fun getTradeById(id: String): Trade?
    suspend fun saveTrade(trade: Trade)
    suspend fun deleteTrade(id: String)
}