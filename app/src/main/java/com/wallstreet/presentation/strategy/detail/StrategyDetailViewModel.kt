package com.wallstreet.presentation.strategy.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallstreet.domain.model.TimePeriod
import com.wallstreet.domain.repository.AuthRepository
import com.wallstreet.domain.usecase.analytics.GetDayPerformanceUseCase
import com.wallstreet.domain.usecase.analytics.GetTradeSummaryUseCase
import com.wallstreet.domain.usecase.analytics.GetTrendPerformanceUseCase
import com.wallstreet.domain.usecase.home.GetEquityCurveDataUsecase
import com.wallstreet.domain.usecase.home.GetHomeStateUsecase
import com.wallstreet.domain.usecase.home.GetMistakesAnalysisUsecase
import com.wallstreet.domain.usecase.home.GetSymbolPerformanceUsecase
import com.wallstreet.domain.usecase.home.getRecentTradeData
import kotlin.math.max
import com.wallstreet.domain.usecase.strategy.GetStrategyUseCase
import com.wallstreet.domain.usecase.trade.GetTradesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class StrategyDetailViewModel(
    private val strategyId: String,
    private val authRepository: AuthRepository,
    private val getTradesUsecase: GetTradesUseCase,
    private val getStrategyUseCase: GetStrategyUseCase,
    private val getHomeStateUsecase: GetHomeStateUsecase,
    private val getTradeSummaryUseCase: GetTradeSummaryUseCase,
    private val getDayPerformanceUseCase: GetDayPerformanceUseCase,
    private val equityCurveDataUsecase: GetEquityCurveDataUsecase,
    private val mistakesAnalysisUsecase: GetMistakesAnalysisUsecase,
    private val symbolPerformanceUsecase: GetSymbolPerformanceUsecase,
    private val getTrendPerformanceUseCase: GetTrendPerformanceUseCase,
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(TimePeriod.ONE_MONTH)
    val selectedPeriod: StateFlow<TimePeriod> = _selectedPeriod.asStateFlow()

    val uiState: StateFlow<StrategyDetailUiState> = _selectedPeriod
        .flatMapLatest { period ->
            val userId = authRepository.getCurrentUser()?.id
            if (userId.isNullOrBlank()) {
                return@flatMapLatest flowOf(
                    StrategyDetailUiState.Error("Not signed in")
                )
            }

            combine(
                getTradesUsecase(userId, period, 500),
                getStrategyUseCase()
            ) { trades, strategies ->
                val strategy = strategies.firstOrNull { it.id == strategyId }
                    ?: return@combine StrategyDetailUiState.Error("Strategy not found")

                val strategyTrades = trades.filter { it.strategyId == strategyId }
                val sorted = strategyTrades.sortedBy { it.tradeDate }

                val grossWin = sorted.filter { (it.profitLoss ?: 0.0) > 0 }.sumOf { it.profitLoss ?: 0.0 }
                val grossLoss = sorted.filter { (it.profitLoss ?: 0.0) < 0 }.sumOf { -(it.profitLoss ?: 0.0) }
                val profitFactor = if (grossLoss > 0) grossWin / grossLoss else if (grossWin > 0) 999.0 else 0.0

                var equity = 0.0
                var peak = 0.0
                var maxDrawdown = 0.0
                for (t in sorted) {
                    equity += t.profitLoss ?: 0.0
                    peak = max(peak, equity)
                    maxDrawdown = max(maxDrawdown, peak - equity)
                }

                var currentStreak = 0
                var winStreak = 0
                for (t in sorted) {
                    val pnl = t.profitLoss ?: 0.0
                    if (pnl > 0) {
                        currentStreak++
                        winStreak = max(winStreak, currentStreak)
                    } else if (pnl < 0) {
                        currentStreak = 0
                    }
                }

                StrategyDetailUiState.Success(
                    strategy = strategy,
                    selectedPeriod = period,
                    stats = getHomeStateUsecase(strategyTrades),
                    tradeSummary = getTradeSummaryUseCase(strategyTrades),
                    dayPerformance = getDayPerformanceUseCase(strategyTrades),
                    equityCurveData = equityCurveDataUsecase(strategyTrades),
                    mistakesAnalysisData = mistakesAnalysisUsecase(strategyTrades),
                    symbolPerformance = symbolPerformanceUsecase(strategyTrades),
                    recentTrades = getRecentTradeData(strategyTrades),
                    totalTradesInPeriod = strategyTrades.size,
                    profitFactor = profitFactor,
                    maxDrawdown = maxDrawdown,
                    winStreak = winStreak,
                    trendPerformance = getTrendPerformanceUseCase(strategyTrades)
                )
            }
        }
        .flowOn(Dispatchers.Default)
        .onStart { emit(StrategyDetailUiState.Loading) }
        .catch { e -> emit(StrategyDetailUiState.Error(e.message ?: "Unknown error")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StrategyDetailUiState.Loading
        )

    fun onPeriodSelected(period: TimePeriod) {
        _selectedPeriod.value = period
    }
}
