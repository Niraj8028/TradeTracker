package com.wallstreet.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallstreet.domain.model.TimePeriod
import com.wallstreet.domain.repository.AuthRepository
import com.wallstreet.domain.usecase.home.ComputeHeatMapDataUsecase
import com.wallstreet.domain.usecase.home.GetEquityCurveDataUsecase
import com.wallstreet.domain.usecase.home.GetHomeStateUsecase
import com.wallstreet.domain.usecase.home.GetMistakesAnalysisUsecase
import com.wallstreet.domain.usecase.home.GetSymbolPerformanceUsecase
import com.wallstreet.domain.usecase.home.RecentTradesDataUsecase
import com.wallstreet.domain.usecase.home.getRecentTradeData
import com.wallstreet.domain.usecase.trade.GetTradesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    private val getTradesUsecase: GetTradesUseCase,
    private val getHomeStateUsecase: GetHomeStateUsecase,
    private val heatMapDataUsecase: ComputeHeatMapDataUsecase,
    private val recentTradesDataUsecase: RecentTradesDataUsecase,
    private val equityCurveDataUsecase: GetEquityCurveDataUsecase,
    private val mistakesAnalysisUsecase: GetMistakesAnalysisUsecase,
    private val symbolPerformanceUsecase: GetSymbolPerformanceUsecase,
    private val authRepository: AuthRepository
) : ViewModel() {

    val selectedPeriod = MutableStateFlow(TimePeriod.ONE_MONTH)

    val homeUiState: StateFlow<HomeUiState> = selectedPeriod
        .flatMapLatest { period ->
            val userId = authRepository.getCurrentUser()!!.id
            getTradesUsecase(userId, period, 500)
                .map { trades ->
                    HomeUiState.Success(
                        stats = getHomeStateUsecase(trades),
                        recentTrades = getRecentTradeData(trades),
                        heatMapData = heatMapDataUsecase(trades, 4),
                        selectedPeriod = period,
                        equityCurveData = equityCurveDataUsecase(trades),
                        mistakesAnalysisData = mistakesAnalysisUsecase(trades),
                        symbolPerformance = symbolPerformanceUsecase(trades)
                    ) as HomeUiState
                }
                .flowOn(Dispatchers.Default)
                .catch { e ->
                    emit(HomeUiState.Error(e.message ?: "Unknown error"))
                }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState.Loading
        )

    fun onPeriodSelected(period: TimePeriod) {
        selectedPeriod.value = period
    }

}
