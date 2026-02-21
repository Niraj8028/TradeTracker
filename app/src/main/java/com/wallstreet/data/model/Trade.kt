package com.wallstreet.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.type.Date
import com.wallstreet.domain.model.TradeType

data class TradeDto(
    @DocumentId
    val id: String = "",
    val symbol: String = "",
    val entryPrice: Double = 0.0,
    val exitPrice: Double? = null,
    val quantity: Double = 0.0,
    val tradeType: TradeType = TradeType.LONG,
    // TODO add date
//    val date: Date,
    val profitLoss: Double? = null,
    val profitLossPercentage: Double? = null,
    val strategy: String = "",
    val notes: String = "",
    val imageUrl: String? = null,
    val userId: String = "",
    val comments: String? = null,
    // TODO work on mistakes model
    val mistakes: List<String> = emptyList(),
    val createdAt: Timestamp = Timestamp.now()
) {
    fun calculateProfitLoss(): Double {
        return if (exitPrice != null) {
            when(tradeType) {
                TradeType.LONG -> (exitPrice - entryPrice) * quantity
                TradeType.SHORT -> (entryPrice - exitPrice) * quantity
            }
        } else {
            0.0
        }
    }
}


