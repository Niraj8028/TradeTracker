package com.wallstreet.domain.usecase.home

import com.wallstreet.domain.model.EquityCurveData
import com.wallstreet.domain.model.EquityPoint
import com.wallstreet.domain.model.Trade
import java.text.SimpleDateFormat
import java.util.Calendar
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
            .mapValues { (_, dayTrades) -> dayTrades.sumOf { it.profitLoss ?: 0.0 } }

        val sortedDates = trades.map { it.tradeDate }.sorted()

        val startCal = Calendar.getInstance().apply {
            timeInMillis = sortedDates.first()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endCal = Calendar.getInstance().apply {
            timeInMillis = sortedDates.last()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        var cumulativePnL = 0.0
        var peak = 0.0
        var maxDrawdown = 0.0
        var maxDrawdownDate: Long? = null
        val points = mutableListOf<EquityPoint>()

        while (!startCal.after(endCal)) {
            val dateKey = dateFormat.format(startCal.time)
            val dayPnL = dailyPnL[dateKey] ?: 0.0
            cumulativePnL += dayPnL

            if (cumulativePnL > peak) {
                peak = cumulativePnL
            }
            val currentDrawdown = peak - cumulativePnL
            if (currentDrawdown > maxDrawdown) {
                maxDrawdown = currentDrawdown
                maxDrawdownDate = startCal.timeInMillis
            }

            points.add(EquityPoint(date = startCal.timeInMillis, cumulativePnL = cumulativePnL))
            startCal.add(Calendar.DAY_OF_YEAR, 1)
        }

        return EquityCurveData(
            points = points,
            totalPnL = cumulativePnL,
            maxDrawdown = maxDrawdown,
            maxDrawdownDate = maxDrawdownDate
        )
    }
}