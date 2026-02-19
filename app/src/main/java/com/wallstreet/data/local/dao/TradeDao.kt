package com.wallstreet.data.local.dao

import androidx.room.*
import com.wallstreet.data.local.entity.TradeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TradeDao {
    @Query("SELECT * FROM trades ORDER BY date DESC")
    fun getAll(): Flow<List<TradeEntity>>

    @Query("SELECT * FROM trades WHERE assetType = :assetType ORDER BY date DESC")
    fun getByAssetType(assetType: String): Flow<List<TradeEntity>>

    @Query("SELECT * FROM trades ORDER BY date DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<TradeEntity>>

    @Query("SELECT * FROM trades WHERE id = :id")
    suspend fun getById(id: String): TradeEntity?

    @Query("SELECT * FROM trades WHERE synced = 0")
    suspend fun getUnsynced(): List<TradeEntity>

    @Upsert
    suspend fun upsert(trade: TradeEntity)

    @Query("DELETE FROM trades WHERE id = :id")
    suspend fun delete(id: String)
}