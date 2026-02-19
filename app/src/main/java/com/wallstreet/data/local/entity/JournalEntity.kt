package com.wallstreet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "journal_entries")
data class JournalEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val tradeId: String? = null,
    val date: LocalDate,
    val psychologyNote: String,
    val images: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val synced: Boolean = false
)