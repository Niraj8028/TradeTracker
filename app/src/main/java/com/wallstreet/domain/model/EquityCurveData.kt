package com.wallstreet.domain.model

data class EquityCurveData(
    val points: List<EquityPoint>,
    val totalPnL: Double,
    val maxDrawdown: Double,
    val maxDrawdownDate: Long?
)

data class EquityPoint(
    val date: Long,
    val cumulativePnL: Double
)
