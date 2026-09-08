package com.wallstreet.presentation.home

import com.google.android.gms.common.internal.Objects
import com.wallstreet.domain.model.EquityCurveData
import com.wallstreet.domain.model.HeatMapData
import com.wallstreet.domain.model.HomeStats
import com.wallstreet.domain.model.MistakesAnalysisData
import com.wallstreet.domain.model.RecentTradeItem
import com.wallstreet.domain.model.SymbolStat
import com.wallstreet.domain.model.TimePeriod
import com.wallstreet.domain.model.Trade

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Error(val error: String) : HomeUiState()
    data class Success(
        val stats: HomeStats,
        val recentTrades: List<RecentTradeItem>,
        val heatMapData: HeatMapData,
        val selectedPeriod: TimePeriod,
        val equityCurveData: EquityCurveData,
        val mistakesAnalysisData: MistakesAnalysisData,
        val symbolPerformance: List<SymbolStat>
    ) : HomeUiState()
}

