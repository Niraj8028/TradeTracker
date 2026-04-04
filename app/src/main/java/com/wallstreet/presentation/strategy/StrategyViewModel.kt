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

   
    private val _strategies = MutableStateFlow<List<Strategy>>(emptyList())
    val strategies: StateFlow<List<Strategy>> = _strategies.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        observeStrategies()
    }

    private fun observeStrategies() {
        viewModelScope.launch {
            getStrategyUseCase().collect {
                _strategies.value = it
                _isLoading.value = false
            }
        }
    }

    fun addStrategy(strategy: Strategy) {
        viewModelScope.launch {
            _isLoading.value = true

            when (val result = addStrategyUseCase(strategy)) {
                is

                Result.Success -> {
                    _isLoading.value = false
                }

                is Result.Error -> {
                    _error.value = result.message
                    _isLoading.value = false
                }

                else -> {}
            }
        }
    }

    fun deleteStrategy(strategy: Strategy) {
        viewModelScope.launch {
            deleteStrategyUseCase(strategy)
        }
    }

    fun updateStrategy(strategy: Strategy) {
        viewModelScope.launch {
            updateStrategyUseCase(strategy)
        }
    }

    fun clearError() {
        _error.value = null
    }
}