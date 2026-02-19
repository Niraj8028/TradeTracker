package com.wallstreet.domain.model

data class EquityMetrics(
    val currentEquity: Double,
    val topProfit: Double,
    val openRisk: Double,
    val sharpeRatio: Double,
    val recoveryFactor: Double,
    val maxDrawdown: Double,
    val profitFactor: Double,
    val standardDeviation: Double,
    val calmarRatio: Double,
    val avgMonthlyGrowth: Double
)