package com.wallstreet.domain.model

data class MistakeStat(
    val name: String,
    val count: Int,
    val totalPnlImpact: Double,
    val avgPnlImpact: Double,
    val winRate: Double
    )