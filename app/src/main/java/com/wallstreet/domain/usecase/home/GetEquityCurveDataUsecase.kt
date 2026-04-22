package com.wallstreet.domain.usecase.home

import com.wallstreet.domain.model.EquityCurveData
import com.wallstreet.domain.model.EquityPoint
import com.wallstreet.domain.model.Trade
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GetEquityCurveDataUsecase {
    operator fun invoke(trades: List<Trade>): EquityCurveData {
        return computeEquityCurveData(trades);
    }

    private fun computeEquityCurveData(trades: List<Trade>): EquityCurveData {
        if (trades.isEmpty()) {
            return EquityCurveData(
                points = emptyList(),
                totalPnL = 0.0,
                maxDrawdown = 0.0,
                maxDrawdownDate = null
            )
        }

        // Group trades by date (strip time, keep only date)
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val dailyPnL = trades
            .groupBy { dateFormat.format(Date(it.tradeDate)) }
            .toSortedMap()
            .map { (dateKey, dayTrades) ->
                val date = dateFormat.parse(dateKey)!!.time
                val dayTotal = dayTrades.sumOf { it.profitLoss ?: 0.0 }
                date to dayTotal
            }

        var cumulativePnL = 0.0
        var peak = 0.0
        var maxDrawdown = 0.0
        var maxDrawdownDate: Long? = null

        val points = dailyPnL.map { (date, dayPnL) ->
            cumulativePnL += dayPnL

            if (cumulativePnL > peak) {
                peak = cumulativePnL
            }

            val currentDrawdown = peak - cumulativePnL
            if (currentDrawdown > maxDrawdown) {
                maxDrawdown = currentDrawdown
                maxDrawdownDate = date
            }

            EquityPoint(
                date = date,
                cumulativePnL = cumulativePnL
            )
        }

        return EquityCurveData(
            points = points,
            totalPnL = cumulativePnL,
            maxDrawdown = maxDrawdown,
            maxDrawdownDate = maxDrawdownDate
        )
    }
}