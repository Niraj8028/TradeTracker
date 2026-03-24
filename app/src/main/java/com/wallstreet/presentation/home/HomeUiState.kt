package com.wallstreet.presentation.home

import com.google.android.gms.common.internal.Objects
import com.wallstreet.domain.model.HeatMapData
import com.wallstreet.domain.model.HomeStats
import com.wallstreet.domain.model.Trade

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Error(val error: String): HomeUiState()
    data class Success(
        val stats: HomeStats,
        val trades: List<Trade>,
        val heatMapData: HeatMapData
    ): HomeUiState()
}

enum class TimePeriod(val label: String) {
    ONE_WEEk("1W"),
    ONE_MONTH("1M"),
    THREE_MONTHS("3M"),
    SIX_MONTHS("6M"),
    ONE_YEAR("1Y"),
    ALL("ALL")
}
