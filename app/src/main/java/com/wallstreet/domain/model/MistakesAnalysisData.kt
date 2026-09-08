package com.wallstreet.domain.model

data class MistakesAnalysisData(
    val topMistakes: List<MistakeStat>,
    val mostCostlyMistakes: MistakeStat,
    val totalMistakeTrades: Int,
    val cleanTradeWinRate: Double
    )