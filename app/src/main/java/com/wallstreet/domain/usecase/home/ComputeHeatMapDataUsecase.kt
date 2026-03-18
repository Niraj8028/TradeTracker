package com.wallstreet.domain.usecase.home

import com.wallstreet.domain.model.HeatMapData
import com.wallstreet.domain.model.Trade
import java.time.LocalDate

class ComputeHeatMapDataUsecase {
    operator fun invoke(trades: List<Trade>, weeks: Int = 5): HeatMapData {
        return buildHeatMapData(trades, weeks);
    }

    private fun buildHeatMapData(trades: List<Trade>, weeks: Int): HeatMapData {
        val today = LocalDate.now();
        val mostRecentMonday = today.with(java.time.DayOfWeek.MONDAY)
        val gridStart = mostRecentMonday.minusWeeks((weeks -1).toLong())
        val gridEnd = mostRecentMonday.plusDays(6)

//        val dailyMap: List<LocalDate, Pair<Double, Int>> = trades
//            .
        return HeatMapData(
            weeks = emptyList(),
            maxAbsPnl = 0.0
        )
    }
}