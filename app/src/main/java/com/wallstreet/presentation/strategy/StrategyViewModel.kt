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
import com.wallstreet.domain.repository.AuthRepository
import com.wallstreet.presentation.home.TimePeriod
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

class StrategyViewModel(
    private val updateStrategyUseCase: UpdateStrategyUseCase,
    private val deleteStrategyUseCase: DeleteStrategyUseCase,
    private val addStrategyUseCase: AddStrategyUseCase,
    private val getStrategyStatsUsecase: GetStrategyStatsUsecase,
    private val authRepository: AuthRepository
    ) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(TimePeriod.ONE_MONTH)
    val selectedPeriod: StateFlow<TimePeriod> = _selectedPeriod.asStateFlow()

    private val _actionState = MutableStateFlow<ActionState>(ActionState.Idle)
    val actionState: StateFlow<ActionState> = _actionState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<StrategiesUiState> = _selectedPeriod.flatMapLatest { period ->
        val userId = authRepository.getCurrentUser()!!.id;
        getStrategyStatsUsecase(userId, period).map { stats ->
            StrategiesUiState.Success(
                strategyStats = stats,
                selectedPeriod = period
            ) as StrategiesUiState
        }.onStart { emit(StrategiesUiState.Loading) }
            .catch { e ->
                emit(StrategiesUiState.Error(e.message ?: "Unknown error"))
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StrategiesUiState.Loading
    )

    fun onPeriodSelected(period: TimePeriod) {
        _selectedPeriod.value = period
    }


    fun addStrategy(name: String, description: String?) {
        if(name.isBlank()) {
            _actionState.value = ActionState.ValidationError("Name cannot be empty")
            return
        }
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
             val result = addStrategyUseCase(
                Strategy(
                    name = name,
                    isCustom = true,
                )
            )
            _actionState.value = when(result) {
                is Result.Error -> ActionState.Error(result.message ?: "Failed to add strategy")
                Result.Loading -> ActionState.Loading
                is Result.Success<*> -> ActionState.Success
            }
        }
    }

    fun deleteStrategy(strategy: Strategy) {
        if (strategy.id.isEmpty()) {
            _actionState.value = ActionState.ValidationError("Invalid strategy id")
            return
        }
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            _actionState.value = when (val result = deleteStrategyUseCase(strategy)) {
                is Result.Success -> ActionState.Success
                is Result.Error -> ActionState.Error(result.message ?: "Failed to delete")
                else -> ActionState.Idle
            }
        }
    }

    fun updateStrategy(strategy: Strategy) {
        if (strategy.id.isEmpty()) {
            _actionState.value = ActionState.ValidationError("Invalid strategy id")
            return
        }
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            _actionState.value = when (val result = updateStrategyUseCase(strategy)) {
                is Result.Success -> ActionState.Success
                is Result.Error -> ActionState.Error(result.message ?: "Failed to update")
                else -> ActionState.Idle
            }
        }
    }

    fun clearActionState() {
        _actionState.value = ActionState.Idle
    }
}