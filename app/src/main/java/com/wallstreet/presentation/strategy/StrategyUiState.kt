package com.wallstreet.presentation.strategy

import com.wallstreet.domain.model.strategy.StrategyStats
import com.wallstreet.domain.model.TimePeriod

sealed interface StrategiesUiState {
    data object Loading : StrategiesUiState
    data class Error(val message: String) : StrategiesUiState
    data class Success(
        val strategyStats: List<StrategyStats>,
        val selectedPeriod: TimePeriod
    ): StrategiesUiState
}

sealed interface ActionState {
    data object Idle : ActionState
    data object Loading : ActionState
    data object Success : ActionState
    data class Error(val message: String) : ActionState
    data class ValidationError(val message: String) : ActionState
}