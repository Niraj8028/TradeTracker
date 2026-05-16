package com.wallstreet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wallstreet.domain.model.TradeType

enum class SyncStatus { PENDING, SYNCED, FAILED }

@Entity(tableName = "trades")
data class TradeEntity(
    @PrimaryKey val id: String,
    val symbol: String = "",
    val entryPrice: Double = 0.0,
    val exitPrice: Double? = null,
    val quantity: Double = 0.0,
    val tradeType: TradeType = TradeType.LONG,
    val strategyId: String? = null,
    val tradeDate: Long = System.currentTimeMillis(),
    val profitLoss: Double? = null,
    val profitLossPercentage: Double? = null,
    val strategy: String = "",
    val notes: String = "",
    val imageUrl: String? = null,
    val userId: String = "",
    val comments: String? = null,
    val mistakes: List<String> = emptyList(),
    val createdAt: Long? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val lastSyncAttempt: Long? = null,
    val syncError: String? = null
)