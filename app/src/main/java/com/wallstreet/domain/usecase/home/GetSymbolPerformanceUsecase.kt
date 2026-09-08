package com.wallstreet.domain.usecase.home

import com.wallstreet.domain.model.SymbolStat
import com.wallstreet.domain.model.Trade

private const val TOP_N = 5

class GetSymbolPerformanceUsecase {
    operator fun invoke(trades: List<Trade>): List<SymbolStat> {
        val closed = trades.filter { it.profitLoss != null && it.symbol.isNotBlank() }
        if (closed.isEmpty()) return emptyList()

        val stats = closed
            .groupBy { it.symbol }
            .map { (symbol, group) ->
                val totalPnl = group.sumOf { it.profitLoss ?: 0.0 }
                val wins = group.count { (it.profitLoss ?: 0.0) >= 0.0 }
                val winRate = (wins.toDouble() / group.size) * 100.0
                SymbolStat(
                    symbol = symbol,
                    tradeCount = group.size,
                    totalPnl = totalPnl,
                    winRate = winRate
                )
            }
            .sortedByDescending { it.totalPnl }
            .take(TOP_N)

        return stats.mapIndexed { index, stat ->
            stat.copy(
                isBest = index == 0 && stat.totalPnl > 0,
                isWorst = index == stats.lastIndex && stat.totalPnl < 0
            )
        }
    }
}
