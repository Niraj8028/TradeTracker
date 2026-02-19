package com.wallstreet.domain.model

import java.time.LocalDate

data class JournalEntry(
    val id: String,
    val userId: String,
    val tradeId: String? = null,
    val date: LocalDate,
    val psychologyNote: String,
    val images: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val synced: Boolean = false
)