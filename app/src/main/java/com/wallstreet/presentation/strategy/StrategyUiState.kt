package com.wallstreet.presentation.strategy

import com.wallstreet.domain.model.strategy.StrategyStats
import com.wallstreet.domain.model.TimePeriod
import com.wallstreet.domain.model.insights.Insight
import com.wallstreet.domain.model.insights.StrategyVerdict

sealed interface StrategiesUiState {
    data object Loading : StrategiesUiState
    data class Error(val message: String) : StrategiesUiState
    data class Success(
        val strategyStats: List<StrategyStats>,
        val selectedPeriod: TimePeriod,
        val strategyInsights: List<Insight> = emptyList(),
        val verdictByStrategyId: Map<String, StrategyVerdict> = emptyMap(),
    ) : StrategiesUiState
}

enum class StrategySortOption(val label: String) {
    PNL("P&L"),
    WIN_RATE("Win Rate"),
    TRADES("Trades"),
    RR_RATIO("R:R"),
    RECENT("Recent")
}

enum class SortDirection { ASC, DESC }

sealed interface ActionState {
    data object Idle : ActionState
    data object Loading : ActionState
    data object Success : ActionState
    data class Error(val message: String) : ActionState
    data class ValidationError(val message: String) : ActionState
}
