//// TODO rename to User strategy
data class strategyDto(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val description: String = "",
    val isCustom: String = "",
    val createAt: Long? = null
//    val totalTrades: Int = 0,
//    val winRate: Double = 0.0,
//    val profitLoss: Double = 0.0,
//    val profitLossPercentage: Double = 0.0,
//    val riskRewardRatio: Double = 0.0,
//    var isArchived: Boolean = false
)