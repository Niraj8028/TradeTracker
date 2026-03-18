package com.wallstreet.presentation.log_trade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallstreet.core.result.Result
import com.wallstreet.domain.model.Trade
import com.wallstreet.domain.model.TradeType
import com.wallstreet.domain.repository.AuthRepository
import com.wallstreet.domain.usecase.trade.AddTradeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LogTradeViewModel(
    private val addTradeUseCase: AddTradeUseCase,

    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogTradeUiState())
    val uiState: StateFlow<LogTradeUiState> = _uiState.asStateFlow()

    // TODO fetch this from backend
    val strategies = listOf(
        "Bull Flag Breakout",
        "Support/Resistance Bounce",
        "Moving Average Crossover",
        "VWAP Reversion",
        "Gap Fill Strategy",
        "Trend Following"
    )

    val mistakes = listOf(
        "FOMO",
        "Early Exit",
        "Large Size",
        "Revenge Trade",
        "No Stop Loss",
        "No Setup",
    )

    fun onImageSelected(uri: String) {
        _uiState.value = _uiState.value.copy(imageUri = uri);
    }

    fun onTradeTypeChanged(tradeType: TradeType) {
        clearError()
        _uiState.value = _uiState.value.copy(tradeType = tradeType)
    }

    fun onSymbolChanged(symbol: String) {
        _uiState.value = _uiState.value.copy(symbol = symbol)
    }

    fun onQuantityChanged(quantity: String) {
        if (isValidDecimalInput(quantity)) {
            _uiState.value = _uiState.value.copy(
                quantity = quantity,
                quantityError = null
            )
        }
    }

    fun onEntryPriceChanged(price: String) {
        if (isValidDecimalInput(price)) {
            _uiState.value = _uiState.value.copy(
                entryPrice = price,
                entryPriceError = null
            )
        }
    }

    fun onExitPriceChanged(price: String) {
        if (isValidDecimalInput(price)) {
            _uiState.value = _uiState.value.copy(
                exitPrice = price,
                exitPriceError = null
            )
        }
    }

    fun onStrategySelected(strategy: String) {
        _uiState.value = _uiState.value.copy(selectedStrategy = strategy)
    }

    fun onMistakeToggled(mistake: String) {
        val currentMistakes = _uiState.value.selectedMistakes;
        val newMistakes = if (currentMistakes.contains(mistake)) {
            currentMistakes - mistake
        } else {
            currentMistakes + mistake
        }
        _uiState.value = _uiState.value.copy(selectedMistakes = newMistakes)
    }

    fun onStopLossChanged(value: String) {
        _uiState.update { it.copy(stopLoss = value) }
    }

    fun onSaveTrade() {
        val user = authRepository.getCurrentUser()

        if (user == null) {
            _uiState.update { it.copy(error = "User not logged in") }
            return
        }
        if (!validateForm()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true);
            val state = _uiState.value;
            val currentTime = System.currentTimeMillis()
            val trade = Trade(
                id = "",
                userId = user.id,
                symbol = state.symbol,
                entryPrice = state.entryPrice.toDouble(),
                exitPrice = state.exitPrice.toDoubleOrNull(),
                quantity = state.quantity.toDouble(),
                tradeType = state.tradeType,
                profitLoss = calculateProfitLoss(
                    state.entryPrice.toDouble(),
                    state.exitPrice.toDoubleOrNull(),
                    state.quantity.toDouble(),
                    state.tradeType
                ),
                profitLossPercentage = calculateProfitLossPercentage(
                    state.entryPrice.toDouble(),
                    state.exitPrice.toDoubleOrNull(),
                    state.tradeType
                ),
                strategy = state.selectedStrategy,
                notes = "",
                imageUrl = state.imageUri,
                createAt = currentTime,
                mistakes = state.selectedMistakes.toList(),
                comments = "",
            )
            when (val result = addTradeUseCase.invoke(trade)) {
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.message,
                        isLoading = false,
                    )

                }

                Result.Loading -> TODO()
                is Result.Success -> {
                    _uiState.value = LogTradeUiState()
                    _uiState.value = _uiState.value.copy(
                        success = true,
                        isLoading = false
                    )
                }
            }
        }
//        clearError()
    }

    private fun validateForm(): Boolean {
        val state = _uiState.value
        var isValid = true
        if (state.symbol.isBlank()) {
            _uiState.value = _uiState.value.copy(
                symbolError = "Symbol is required"
            )
            isValid = false
        }
        if (state.quantity.isBlank() || state.quantity.toDoubleOrNull() == null || state.quantity.toDouble() < 0.01) {
            _uiState.value = _uiState.value.copy(quantityError = "Quantity must be at least 0.01")
            isValid = false
        }
        if (state.entryPrice.isBlank() || state.entryPrice.toDoubleOrNull() == null || state.entryPrice.toDouble() <= 0) {
            _uiState.value =
                _uiState.value.copy(entryPriceError = "Entry price must be greater than 0")
            isValid = false
        }
        if (state.exitPrice.isNotEmpty()) {
            val exitPriceValue = state.exitPrice.toDoubleOrNull()
            if (exitPriceValue == null || exitPriceValue <= 0) {
                _uiState.value =
                    _uiState.value.copy(exitPriceError = "Exit price must be greater than 0")
                isValid = false
            }
        }

        return isValid
    }

    private fun calculateProfitLoss(
        entryPrice: Double,
        exitPrice: Double?,
        quantity: Double,
        tradeType: TradeType
    ): Double? {
        if (exitPrice == null) return null
        return when (tradeType) {
            TradeType.LONG -> ((exitPrice - entryPrice) / entryPrice) * quantity
            TradeType.SHORT -> ((entryPrice - exitPrice) / entryPrice) * quantity
        }
    }

    private fun calculateProfitLossPercentage(
        entryPrice: Double,
        exitPrice: Double?,
        tradeType: TradeType
    ): Double? {
        if (exitPrice == null) return null

        return when (tradeType) {
            TradeType.LONG -> ((exitPrice - entryPrice) / entryPrice) * 100
            TradeType.SHORT -> ((entryPrice - exitPrice) / entryPrice) * 100
        }
    }

    fun onCommentsAdded(comment: String) {
        _uiState.value = _uiState.value.copy(
            comments = comment
        )
    }

    private fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
        _uiState.value = _uiState.value.copy(
            exitPriceError = null,
            entryPriceError = null,
            error = null,
            symbolError = null,
            quantityError = null
        )
    }

    private fun isValidDecimalInput(input: String): Boolean {
        if (input.isEmpty()) return true

        // Regex explanation:
        // ^\d* - starts with zero or more digits
        // \.? - optionally followed by a decimal point
        // \d{0,2}$ - ends with 0 to 2 digits (for 2 decimal places)
        val regex = Regex("^\\d*\\.?\\d{0,2}\$")
        return input.matches(regex)
    }

}