package com.wallstreet.presentation.log_trade

import com.wallstreet.domain.model.Strategy
import com.wallstreet.domain.model.TradeType

data class LogTradeUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,

    // Form fields
    val tradeType: TradeType = TradeType.LONG,
    val symbol: String = "",
    val quantity: String = "",
    val entryPrice: String = "",
    val exitPrice: String = "",
    val selectedStrategy: Strategy? = null,
    val comments: String = "",
    val selectedMistakes: Set<String> = emptySet(),
    val imageUri: String? = null,
    val tradeDate: Long = System.currentTimeMillis(),
    val strategies: List<Strategy> = emptyList(),
    // Validation
    val symbolError: String? = null,
    val quantityError: String? = null,
    val entryPriceError: String? = null,
    val exitPriceError: String? = null,
    val stopLoss: String? = null,

    )
