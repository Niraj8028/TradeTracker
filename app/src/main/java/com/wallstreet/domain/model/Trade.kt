package com.wallstreet.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.type.DateTime

data class Trade(
    @DocumentId
    val id: String = "",
    val symbol: String = "",
    val entryPrice: Double = 0.0,
    val exitPrice: Double? = null,
    val quantity: Double = 0.0,
    val strategyId: String? = null,
    val tradeType: TradeType = TradeType.LONG,
    // TODO add date
    val tradeDate: Long = System.currentTimeMillis(),
    val profitLoss: Double? = null,
    val profitLossPercentage: Double? = null,
    val strategy: String = "",
    val notes: String = "",
    val imageUrl: String? = null,
    val userId: String = "",
    val comments: String? = null,
    // TODO work on mistakes model
    val mistakes: List<String> = emptyList(),
    val createAt: Long? = null,
) {
    fun calculateProfitLoss(): Double? {
        if (exitPrice == null) return null

        return when (tradeType) {
            TradeType.LONG -> (exitPrice - entryPrice) * quantity
            TradeType.SHORT -> (entryPrice - exitPrice) * quantity
        }
    }
}

enum class TradeType {
    LONG, SHORT
}



