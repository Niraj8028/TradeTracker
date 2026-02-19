package com.wallstreet.domain.repository

import com.wallstreet.domain.model.JournalEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface JournalRepository {
    fun getJournalEntries(): Flow<List<JournalEntry>>
    suspend fun getEntryByTradeId(tradeId: String): JournalEntry?
    suspend fun getEntriesByDate(date: LocalDate): List<JournalEntry>
    suspend fun saveEntry(entry: JournalEntry)
}