package com.wallstreet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mistake_tags")
data class MistakeTagEntity(
    @PrimaryKey val id: String,
    val tradeId: String,
    val tag: String,
    val pnlImpact: Double
)