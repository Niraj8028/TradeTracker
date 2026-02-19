package com.wallstreet.data.local.dao

import androidx.room.*
import com.wallstreet.data.local.entity.StrategyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StrategyDao {
    @Query("SELECT * FROM strategies")
    fun getAll(): Flow<List<StrategyEntity>>

    @Query("SELECT * FROM strategies WHERE id = :id")
    suspend fun getById(id: String): StrategyEntity?

    @Upsert
    suspend fun upsert(strategy: StrategyEntity)

    @Query("DELETE FROM strategies WHERE id = :id")
    suspend fun delete(id: String)
}