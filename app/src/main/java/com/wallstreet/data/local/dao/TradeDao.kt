package com.wallstreet.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wallstreet.data.local.entity.SyncStatus
import com.wallstreet.data.local.entity.TradeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TradeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrade(trade: TradeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrades(trades: List<TradeEntity>)

    @Update
    suspend fun updateTrade(trade: TradeEntity)

    @Delete
    suspend fun deleteTrade(trade: TradeEntity)

    @Query("SELECT * FROM trades WHERE userId = :userId ORDER BY tradeDate DESC")
    fun getAllTrades(userId: String): Flow<List<TradeEntity>>

    @Query("SELECT * FROM trades WHERE userId = :userId AND tradeDate >= :fromMillis ORDER BY tradeDate DESC LIMIT :limit")
    suspend fun getRecentTrades(userId: String, fromMillis: Long, limit: Int): List<TradeEntity>

    @Query("SELECT * FROM trades WHERE userId = :userId AND syncStatus != 'SYNCED'")
    suspend fun getPendingSyncTrades(userId: String): List<TradeEntity>

    @Query("SELECT COUNT(*) FROM trades WHERE userId = :userId AND tradeDate >= :startOfDay AND tradeDate <= :endOfDay")
    suspend fun getTradeCountInRange(userId: String, startOfDay: Long, endOfDay: Long): Int

    @Query("UPDATE trades SET syncStatus = :status, lastSyncAttempt = :timestamp, syncError = :error WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: SyncStatus, timestamp: Long, error: String?)

    @Query("DELETE FROM trades WHERE id = :tradeId")
    suspend fun deleteTradeById(tradeId: String)

    @Query("DELETE FROM trades WHERE userId = :userId")
    suspend fun deleteAllTrades(userId: String)
}
