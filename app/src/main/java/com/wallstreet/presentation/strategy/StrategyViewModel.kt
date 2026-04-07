package com.wallstreet.presentation.strategy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallstreet.domain.model.Strategy
import com.wallstreet.domain.usecase.strategy.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.wallstreet.core.result.Result

class StrategyViewModel(
    private val updateStrategyUseCase: UpdateStrategyUseCase,
    private val getStrategyUseCase: GetStrategyUseCase,
    private val deleteStrategyUseCase: DeleteStrategyUseCase,
    private val addStrategyUseCase: AddStrategyUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(StrategyUiState())
    val uiState: StateFlow<StrategyUiState> = _uiState.asStateFlow()


    init {
        observeStrategies()
    }

    private fun observeStrategies() {
        viewModelScope.launch {
            getStrategyUseCase().collect { list ->
                _uiState.value = _uiState.value.copy(
                    strategies = list,
                    isLoading = false
                )
            }
        }
    }

    fun addStrategy(strategy: Strategy) {
        if (strategy.name.isBlank()) {
            _uiState.value = _uiState.value.copy(
                strategyNameError = "Name can not be empty"
            )
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true
            )

            when (val result = addStrategyUseCase(strategy)) {
                is

                Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        success = true,
                        strategyNameError = null
                    )
                }

                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(

                        isLoading = false,
                        error = result.message
                    )
                }

                else -> {}
            }
        }
    }

    fun deleteStrategy(strategy: Strategy) {
        if (strategy.id.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                strategyIdError = "Invalid strategy id"
            )
            return
        }
        viewModelScope.launch {
            deleteStrategyUseCase(strategy)
        }
    }

    fun updateStrategy(strategy: Strategy) {
        if (strategy.id.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                strategyIdError = "Invalid strategy id"
            )
            return
        }
        viewModelScope.launch {
            updateStrategyUseCase(strategy)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(
            error = null
        )

    }
}