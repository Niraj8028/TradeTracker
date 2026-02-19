package com.wallstreet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "trades")
data class TradeEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val symbol: String,
    val side: String,
    val assetType: String,
    val quantity: Double,
    val unit: String,
    val entryPrice: Double,
    val exitPrice: Double,
    val pnl: Double,
    val pnlPercent: Double,
    val date: LocalDate,
    val entryTime: LocalTime? = null,
    val exitTime: LocalTime? = null,
    val strategyId: String? = null,
    val mistakes: List<String> = emptyList(),
    val tradingLogic: String = "",
    val psychologyNote: String = "",
    val attachmentUrls: List<String> = emptyList(),
    val synced: Boolean = false
)