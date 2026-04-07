package com.wallstreet.presentation.strategy

import com.wallstreet.domain.model.Strategy

data class StrategyUiState(

    var isLoading: Boolean = false,
    var success: Boolean = false,
    var error: String? = null,

    //form field
    var strategies: List<Strategy> = emptyList(),
    // Validation

    var strategyNameError: String? = null,
    var strategyIdError: String? = null
)
