package com.wallstreet.domain.usecase.home

import com.wallstreet.domain.model.EquityCurveData
import com.wallstreet.domain.model.EquityPoint
import com.wallstreet.domain.model.Trade

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
        val sortedTrades = trades.sortedBy { it.tradeDate }
        var cumulativePnL = 0.0
        var peak = 0.0
        var maxDrawdown = 0.0
        var maxDrawdownDate: Long? = null

        val points = sortedTrades.map { trade ->
            cumulativePnL += trade.profitLoss ?: 0.0

            // Track peak for drawdown
            if (cumulativePnL > peak) {
                peak = cumulativePnL
            }

            // Calculate drawdown from peak
            val currentDrawdown = peak - cumulativePnL
            if (currentDrawdown > maxDrawdown) {
                maxDrawdown = currentDrawdown
                maxDrawdownDate = trade.tradeDate
            }

            EquityPoint(
                date = trade.tradeDate,
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