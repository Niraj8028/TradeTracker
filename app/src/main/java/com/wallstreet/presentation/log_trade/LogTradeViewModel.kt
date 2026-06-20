package com.wallstreet.presentation.log_trade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallstreet.core.config.RemoteConfigManager
import com.wallstreet.core.result.Result
import com.wallstreet.domain.model.Strategy
import com.wallstreet.domain.model.Trade
import com.wallstreet.domain.model.TrendDirection
import com.wallstreet.domain.model.TradeType
import com.wallstreet.domain.repository.AuthRepository
import com.wallstreet.core.perf.withTrace
import com.wallstreet.domain.usecase.strategy.GetStrategyUseCase
import com.wallstreet.domain.usecase.trade.AddTradeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LogTradeViewModel(
    private val addTradeUseCase: AddTradeUseCase,
    private val authRepository: AuthRepository,
    private val getStrategyUseCase: GetStrategyUseCase,
    private val remoteConfigManager: RemoteConfigManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogTradeUiState())
    val uiState: StateFlow<LogTradeUiState> = _uiState.asStateFlow()

    val mistakes = remoteConfigManager.getMistakeTags()

    private val defaultStrategies = remoteConfigManager.getDefaultStrategies()

    init {
        observeStrategies()
    }

    private fun observeStrategies() {
        viewModelScope.launch {
            getStrategyUseCase().collect { list ->
                // User strategies take precedence; remote defaults fill in the rest.
                val merged = (list + defaultStrategies).distinctBy { it.name }
                _uiState.update { it.copy(strategies = merged) }
            }
        }
    }

    fun onImageSelected(uri: String) {
        _uiState.update { it.copy(imageUri = uri) }
    }

    fun onTradeTypeChanged(tradeType: TradeType) {
        _uiState.update { it.copy(tradeType = tradeType, exitPriceError = null, entryPriceError = null) }
    }

    fun onTrendDirectionSelected(dir: TrendDirection) {
        _uiState.update { state ->
            val newDir = if (state.trendDirection == dir) null else dir
            state.copy(trendDirection = newDir)
        }
    }

    fun onSymbolChanged(symbol: String) {
        _uiState.update { it.copy(symbol = symbol.uppercase(), symbolError = null) }
    }

    fun onQuantityChanged(quantity: String) {
        if (isValidDecimalInput(quantity)) {
            _uiState.update { it.copy(quantity = quantity, quantityError = null) }
        }
    }

    fun onEntryPriceChanged(price: String) {
        if (isValidDecimalInput(price)) {
            _uiState.update { it.copy(entryPrice = price, entryPriceError = null) }
        }
    }

    fun onExitPriceChanged(price: String) {
        if (isValidDecimalInput(price)) {
            _uiState.update { it.copy(exitPrice = price, exitPriceError = null) }
        }
    }

    fun onStrategySelected(strategy: Strategy) {
        _uiState.update { it.copy(selectedStrategy = strategy) }
    }

    fun onMistakeToggled(mistake: String) {
        _uiState.update { state ->
            val updated = if (state.selectedMistakes.contains(mistake)) {
                state.selectedMistakes - mistake
            } else {
                state.selectedMistakes + mistake
            }
            state.copy(selectedMistakes = updated)
        }
    }

    fun onStopLossChanged(value: String) {
        if (isValidDecimalInput(value)) {
            _uiState.update { it.copy(stopLoss = value) }
        }
    }

    fun onDateChange(timestamp: Long) {
        _uiState.update { it.copy(tradeDate = timestamp) }
    }

    fun onCommentsAdded(comment: String) {
        _uiState.update { it.copy(comments = comment) }
    }

    fun onSaveTrade() {
        val user = authRepository.getCurrentUser()
        if (user == null) {
            _uiState.update { it.copy(error = "User not logged in") }
            return
        }
        if (!validateForm()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val state = _uiState.value
            val trade = Trade(
                id = "",
                userId = user.id,
                symbol = state.symbol.uppercase(),
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
                strategyId = state.selectedStrategy?.id,
                strategy = state.selectedStrategy?.name ?: "",
                notes = state.comments,
                comments = state.comments,
                imageUrl = state.imageUri,
                tradeDate = state.tradeDate,
                createAt = System.currentTimeMillis(),
                mistakes = state.selectedMistakes.toList(),
                trendDirection = state.trendDirection
            )
            val result = withTrace("log_trade_save") { trace ->
                trace.putAttribute("has_image", (state.imageUri != null).toString())
                trace.putAttribute("trade_type", state.tradeType.name)
                val r = addTradeUseCase(trade)
                trace.putAttribute("result", if (r is Result.Success) "success" else "error")
                r
            }
            when (result) {
                is Result.Error -> _uiState.update { it.copy(error = result.message, isLoading = false) }
                is Result.Success -> {
                    val strategies = _uiState.value.strategies
                    _uiState.value = LogTradeUiState(strategies = strategies, success = true)
                }
                Result.Loading -> Unit
            }
        }
    }

    private fun validateForm(): Boolean {
        val state = _uiState.value
        var isValid = true

        if (state.symbol.isBlank()) {
            _uiState.update { it.copy(symbolError = "Symbol is required") }
            isValid = false
        }
        if (state.quantity.isBlank() || state.quantity.toDoubleOrNull()?.let { it < 0.01 } != false) {
            _uiState.update { it.copy(quantityError = "Quantity must be at least 0.01") }
            isValid = false
        }
        if (state.entryPrice.isBlank() || state.entryPrice.toDoubleOrNull()?.let { it <= 0 } != false) {
            _uiState.update { it.copy(entryPriceError = "Entry price must be greater than 0") }
            isValid = false
        }
        if (state.exitPrice.isNotEmpty()) {
            val exitVal = state.exitPrice.toDoubleOrNull()
            if (exitVal == null || exitVal <= 0) {
                _uiState.update { it.copy(exitPriceError = "Exit price must be greater than 0") }
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
            TradeType.LONG -> (exitPrice - entryPrice) * quantity
            TradeType.SHORT -> (entryPrice - exitPrice) * quantity
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

    private fun isValidDecimalInput(input: String): Boolean {
        if (input.isEmpty()) return true
        return input.matches(Regex("^\\d*\\.?\\d{0,4}\$"))
    }

    fun resetSuccess() {
        _uiState.update {
            it.copy(success = false)
        }
    }
}
