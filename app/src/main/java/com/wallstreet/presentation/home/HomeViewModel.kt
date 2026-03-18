package com.wallstreet.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallstreet.domain.model.Trade
import com.wallstreet.domain.repository.AuthRepository
import com.wallstreet.domain.repository.TradeRepository
import com.wallstreet.domain.usecase.home.GetHomeStateUsecase
import com.wallstreet.domain.usecase.trade.GetTradesUsecase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn


class HomeViewModel(
    private val getTradesUsecase: GetTradesUsecase,
    private val getHomeStateUsecase: GetHomeStateUsecase,
    private val authRepository: AuthRepository
//    userId: String
): ViewModel() {

    val homeUiState: StateFlow<HomeUiState> =
        getTradesUsecase(
            authRepository.getCurrentUser()!!.id,
            limit = 10
        )
            .map { trades ->
            val stats = getHomeStateUsecase(trades);
            HomeUiState.Success(
                stats = stats,
                trades = trades,
            ) as HomeUiState
        }
            .onStart {
                emit(HomeUiState.Loading)
            }
            .catch { e ->
                emit(HomeUiState.Error(e.message ?: "Unknown error"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = HomeUiState.Loading
            )

}