package com.wallstreet.domain.model

import java.time.LocalDate
import java.time.LocalTime

data class Trade(
    val id: String,
    val userId: String,
    val symbol: String,
    val side: TradeSide,
    val assetType: AssetType,
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
    val mistakes: List<MistakeTag> = emptyList(),
    val tradingLogic: String = "",
    val psychologyNote: String = "",
    val attachmentUrls: List<String> = emptyList(),
    val synced: Boolean = false
)