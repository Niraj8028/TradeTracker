package com.wallstreet.data.mapper

import com.wallstreet.data.local.entity.JournalEntity
import com.wallstreet.domain.model.JournalEntry

fun JournalEntity.toDomain() = JournalEntry(
    id = id, userId = userId, tradeId = tradeId,
    date = date, psychologyNote = psychologyNote,
    images = images, tags = tags, synced = synced
)

fun JournalEntry.toEntity() = JournalEntity(
    id = id, userId = userId, tradeId = tradeId,
    date = date, psychologyNote = psychologyNote,
    images = images, tags = tags, synced = synced
)