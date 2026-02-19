package com.wallstreet.data.local.dao

import androidx.room.*
import com.wallstreet.data.local.entity.JournalEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal_entries ORDER BY date DESC")
    fun getAll(): Flow<List<JournalEntity>>

    @Query("SELECT * FROM journal_entries WHERE tradeId = :tradeId LIMIT 1")
    suspend fun getByTradeId(tradeId: String): JournalEntity?

    @Query("SELECT * FROM journal_entries WHERE date = :date")
    suspend fun getByDate(date: LocalDate): List<JournalEntity>

    @Upsert
    suspend fun upsert(entry: JournalEntity)
}