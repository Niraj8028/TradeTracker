package com.wallstreet.presentation.strategy.detail

import com.wallstreet.domain.model.DayPerformance
import com.wallstreet.domain.model.EquityCurveData
import com.wallstreet.domain.model.HeatMapData
import com.wallstreet.domain.model.HomeStats
import com.wallstreet.domain.model.MistakesAnalysisData
import com.wallstreet.domain.model.RecentTradeItem
import com.wallstreet.domain.model.Strategy
import com.wallstreet.domain.model.SymbolStat
import com.wallstreet.domain.model.TimePeriod
import com.wallstreet.domain.model.TradeSummary
import com.wallstreet.domain.model.TrendPerformanceData

sealed interface StrategyDetailUiState {
    data object Loading : StrategyDetailUiState

    data class Error(val message: String) : StrategyDetailUiState

    data class Success(
        val strategy: Strategy,
        val selectedPeriod: TimePeriod,
        val stats: HomeStats,
        val tradeSummary: TradeSummary,
        val dayPerformance: DayPerformance,
        val equityCurveData: EquityCurveData,
        val mistakesAnalysisData: MistakesAnalysisData,
        val symbolPerformance: List<SymbolStat>,
        val recentTrades: List<RecentTradeItem>,
        val totalTradesInPeriod: Int,
        val profitFactor: Double,
        val maxDrawdown: Double,
        val winStreak: Int,
        val trendPerformance: TrendPerformanceData
    ) : StrategyDetailUiState
}
