package com.wallstreet.domain.usecase.home

import com.wallstreet.domain.model.HeatMapCell
import com.wallstreet.domain.model.HeatMapData
import com.wallstreet.domain.model.HeatMapWeek
import com.wallstreet.domain.model.HeatType
import com.wallstreet.domain.model.Trade
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs

class ComputeHeatMapDataUsecase {
    operator fun invoke(trades: List<Trade>, weeks: Int = 4): HeatMapData {
        return buildHeatMapData(trades, weeks);
    }

    private fun buildHeatMapData(trades: List<Trade>, weeks: Int = 4): HeatMapData {
        val today = LocalDate.now();
        val gridEnd = today.with(java.time.DayOfWeek.SUNDAY)
        val gridStart = gridEnd.minusDays((weeks * 7 - 1).toLong())

        val dailyMap: Map<LocalDate, Pair<Double, Int>> = trades
            .groupBy { trade ->
                Instant.ofEpochMilli(trade.tradeDate)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }
            .mapValues { (_, dayTrades) ->
                val totalPnl = dayTrades.sumOf { it.profitLoss ?: 0.0 }
                val count = dayTrades.size
                totalPnl to count
            }

        val maxAbsPnl = dailyMap.values
            .map { abs(it.first) }
            .maxOrNull()
            ?.takeIf { it > 0.0 } ?: 1.0

        val allDays = generateSequence(gridStart) { it.plusDays(1) }
            .takeWhile { !it.isAfter(gridEnd) }
            .map { date ->
                val (pnl, count) = dailyMap[date] ?: (0.0 to 0)
                val intensity = (abs(pnl) / maxAbsPnl).coerceIn(0.5, 1.0).toFloat()
                val type = when {
                    count == 0 -> HeatType.NEUTRAL
                    pnl > 0    -> HeatType.PROFIT
                    else       -> HeatType.LOSS
                }
                HeatMapCell(
                    date = date,
                    totalPnl = pnl,
                    noOfTrades = count,
                    intensity = if (count == 0) 0f else intensity,
                    type = type
                )
            }
            .toList()

        // Step 5: Chunk into weeks of 7 days each
        val heatmapWeeks = allDays.chunked(7).map { HeatMapWeek(days = it) }

        return HeatMapData(
            weeks = heatmapWeeks,
            maxAbsPnl = maxAbsPnl
        )
    }
}