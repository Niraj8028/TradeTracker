package com.wallstreet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "strategies")
data class StrategyEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val description: String,
    val winRate: Double = 0.0,
    val profitFactor: Double = 0.0,
    val netPnl: Double = 0.0,
    val expectedValue: Double = 0.0,
    val tradeIds: List<String> = emptyList(),
    val synced: Boolean = false
)