package com.wallstreet.presentation.strategy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallstreet.domain.model.Strategy
import com.wallstreet.domain.usecase.strategy.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.wallstreet.core.preferences.CurrencyPreferences
import com.wallstreet.core.result.Result
import com.wallstreet.domain.analytics.AnalyticsManager
import com.wallstreet.domain.repository.AuthRepository
import com.wallstreet.domain.repository.UserRepository
import com.wallstreet.domain.model.TimePeriod
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

class StrategyViewModel(
    private val updateStrategyUseCase: UpdateStrategyUseCase,
    private val deleteStrategyUseCase: DeleteStrategyUseCase,
    private val addStrategyUseCase: AddStrategyUseCase,
    private val getStrategyStatsUsecase: GetStrategyStatsUsecase,
    private val getStrategyInsightsUseCase: GetStrategyInsightsUseCase,
    private val userRepository: UserRepository,
    private val currencyPreferences: CurrencyPreferences,
    private val analyticsManager: AnalyticsManager,
    private val authRepository: AuthRepository,
    private val computeDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {

    private val loggedInsightIds = mutableSetOf<String>()

    private val _selectedPeriod = MutableStateFlow(TimePeriod.ONE_MONTH)
    val selectedPeriod: StateFlow<TimePeriod> = _selectedPeriod.asStateFlow()

    private val _sortOption = MutableStateFlow(StrategySortOption.PNL)
    val sortOption: StateFlow<StrategySortOption> = _sortOption.asStateFlow()

    private val _sortDirection = MutableStateFlow(SortDirection.DESC)
    val sortDirection: StateFlow<SortDirection> = _sortDirection.asStateFlow()

    private val _actionState = MutableStateFlow<ActionState>(ActionState.Idle)
    val actionState: StateFlow<ActionState> = _actionState.asStateFlow()

    private suspend fun personalization(userId: String): Pair<List<String>, String> {
        val roles = (userRepository.getAccountPrefs(userId) as? Result.Success)?.data?.roles.orEmpty()
        val symbol = runCatching { currencyPreferences.currencySymbol.first() }.getOrDefault("$")
        return roles to symbol
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<StrategiesUiState> = combine(
        _selectedPeriod, _sortOption, _sortDirection
    ) { period, sort, dir ->
        Triple(period, sort, dir)
    }.flatMapLatest { (period, sort, dir) ->
        val userId = authRepository.getCurrentUser()?.id
            ?: return@flatMapLatest flowOf(StrategiesUiState.Loading)
        val (roles, symbol) = personalization(userId)
        combine(
            getStrategyStatsUsecase(userId, period),
            getStrategyInsightsUseCase(userId, period, roles, symbol),
        ) { stats, insightsResult ->
            val sorted = when (sort) {
                StrategySortOption.PNL      -> stats.sortedByDescending { it.totalPnl }
                StrategySortOption.WIN_RATE -> stats.sortedByDescending { it.winRate }
                StrategySortOption.TRADES   -> stats.sortedByDescending { it.totalTrades }
                StrategySortOption.RR_RATIO -> stats.sortedByDescending { it.rrRatio }
                StrategySortOption.RECENT   -> stats.sortedByDescending { it.strategy.createAt ?: 0L }
            }
            val directedList = if (dir == SortDirection.DESC) sorted else sorted.reversed()
            val finalList = directedList.filter { it.totalTrades > 0 } +
                            directedList.filter { it.totalTrades == 0 }
            val insights = listOfNotNull(insightsResult.headline) +
                insightsResult.insightsByStrategyId.values.flatten()
                    .distinctBy { it.id }
                    .filter { it.id != insightsResult.headline?.id }
            logNewInsights(insights)
            StrategiesUiState.Success(
                strategyStats = finalList,
                selectedPeriod = period,
                strategyInsights = insights,
                verdictByStrategyId = insightsResult.verdictByStrategyId,
            ) as StrategiesUiState
        }.onStart { emit(StrategiesUiState.Loading) }
            .catch { e -> emit(StrategiesUiState.Error(e.message ?: "Unknown error")) }
    }.flowOn(computeDispatcher).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StrategiesUiState.Loading
    )

    private fun logNewInsights(insights: List<com.wallstreet.domain.model.insights.Insight>) {
        insights.forEach { insight ->
            if (loggedInsightIds.add(insight.id)) {
                analyticsManager.logEvent(
                    "insight_shown",
                    mapOf(
                        "id" to insight.id,
                        "category" to insight.category.name,
                        "severity" to insight.severity.name,
                    ),
                )
            }
        }
    }

    fun onPeriodSelected(period: TimePeriod) {
        _selectedPeriod.value = period
    }

    fun onSortSelected(sort: StrategySortOption) {
        _sortOption.value = sort
    }

    fun onSortDirectionToggled() {
        _sortDirection.value = if (_sortDirection.value == SortDirection.DESC) SortDirection.ASC else SortDirection.DESC
    }

    fun addStrategy(name: String, description: String?) {
        if (name.isBlank()) {
            _actionState.value = ActionState.ValidationError("Name cannot be empty")
            return
        }
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            val result = addStrategyUseCase(Strategy(name = name, isCustom = true))
            _actionState.value = when (result) {
                is Result.Error     -> ActionState.Error(result.message ?: "Failed to add strategy")
                Result.Loading      -> ActionState.Loading
                is Result.Success<*> -> ActionState.Success
            }
        }
    }

    fun deleteStrategies(strategies: List<Strategy>) {
        if (strategies.isEmpty()) {
            _actionState.value = ActionState.ValidationError("No strategies selected")
            return
        }
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            _actionState.value = when (val result = deleteStrategyUseCase(strategies)) {
                is Result.Success -> ActionState.Success
                is Result.Error   -> ActionState.Error(result.message ?: "Failed to delete")
                else              -> ActionState.Idle
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
                is Result.Error   -> ActionState.Error(result.message ?: "Failed to update")
                else              -> ActionState.Idle
            }
        }
    }

    fun clearActionState() {
        _actionState.value = ActionState.Idle
    }
}
