package com.wallstreet.domain.model.insights

import com.wallstreet.core.util.DrawdownResult
import com.wallstreet.domain.model.DayPerformance
import com.wallstreet.domain.model.MistakesAnalysisData
import com.wallstreet.domain.model.OverviewStats
import com.wallstreet.domain.model.Strategy
import com.wallstreet.domain.model.SymbolStat
import com.wallstreet.domain.model.Trade
import com.wallstreet.domain.model.TradeSummary
import com.wallstreet.domain.model.TrendPerformanceData
import com.wallstreet.domain.model.strategy.StrategyStats

/**
 * Everything the detectors need about one time window, precomputed once. Reuses the existing
 * analytics use cases plus [com.wallstreet.core.util.TradeMath] scalars.
 */
data class WindowStats(
    val trades: List<Trade>,
    val closedCount: Int,
    val windowStartMs: Long,
    val windowEndMs: Long,
    val overview: OverviewStats,
    val summary: TradeSummary,
    val trend: TrendPerformanceData,
    val day: DayPerformance,
    val mistakes: MistakesAnalysisData,
    val symbols: List<SymbolStat>,
    val grossProfit: Double,
    val grossLoss: Double,
    val profitFactor: Double?,
    val expectancy: Double,
    val avgWin: Double,
    val avgLoss: Double,
    val maxDrawdown: DrawdownResult,
    val winStreak: Int,
    val lossStreak: Int,
    val currentStreak: Int, // + = wins, − = losses
)

/** Per-strategy stats for one window, used by the strategy detectors. */
data class StrategyWindowStats(
    val strategy: Strategy,
    val stats: StrategyStats,
    val profitFactor: Double?,
    val expectancy: Double,
    val maxDrawdown: Double,
    val winStreak: Int,
    val lossStreak: Int,
    val closedCount: Int,
)
