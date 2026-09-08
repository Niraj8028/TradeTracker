package com.wallstreet.data.model

import com.google.firebase.firestore.DocumentId

//// TODO rename to User strategy
data class strategyDto(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val description: String = "",
    val isCustom: Boolean = false,
    val createAt: Long? = null
//    val totalTrades: Int = 0,
//    val winRate: Double = 0.0,
//    val profitLoss: Double = 0.0,
//    val profitLossPercentage: Double = 0.0,
//    val riskRewardRatio: Double = 0.0,
//    var isArchived: Boolean = false
)