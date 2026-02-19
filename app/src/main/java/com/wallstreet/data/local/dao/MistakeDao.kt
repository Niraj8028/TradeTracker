package com.wallstreet.data.local.dao

import androidx.room.*
import com.wallstreet.data.local.entity.MistakeTagEntity
import kotlinx.coroutines.flow.Flow

// ── Result helper classes ──────────────────────────────────────
data class MistakeFrequency(
    val tag: String,
    val count: Int
)

data class MistakePnlImpact(
    val tag: String,
    val impact: Double
)

// ── DAO ───────────────────────────────────────────────────────
@Dao
interface MistakeDao {

    @Query("SELECT * FROM mistake_tags")
    fun getAll(): Flow<List<MistakeTagEntity>>

    @Query("SELECT tag, COUNT(*) as count FROM mistake_tags GROUP BY tag")
    fun getFrequency(): Flow<List<MistakeFrequency>>        // ← List<DataClass> not Map

    @Query("SELECT tag, SUM(pnlImpact) as impact FROM mistake_tags GROUP BY tag")
    fun getPnlImpact(): Flow<List<MistakePnlImpact>>       // ← List<DataClass> not Map

    @Upsert
    suspend fun upsert(tag: MistakeTagEntity)

    @Query("DELETE FROM mistake_tags WHERE tradeId = :tradeId")
    suspend fun deleteByTradeId(tradeId: String)
}