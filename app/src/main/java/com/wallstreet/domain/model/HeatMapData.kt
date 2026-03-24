package com.wallstreet.domain.model

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

data class HeatMapCell(
    val date: LocalDate,
    val noOfTrades: Int,
    val totalPnl: Double,
    val type: HeatType,
    val intensity: Float,
)

enum class HeatType {
    PROFIT,
    LOSS,
    NEUTRAL
}

data class HeatMapWeek(
    val days: List<HeatMapCell>
)

data class HeatMapData(
    val weeks: List<HeatMapWeek>,
    val maxAbsPnl: Double
)
