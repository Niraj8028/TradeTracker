package com.wallstreet.domain.usecase.strategy

import com.wallstreet.domain.model.Strategy
import com.wallstreet.domain.model.Trade
import com.wallstreet.domain.model.strategy.StrategyStats
import com.wallstreet.domain.repository.StrategyRepository
import com.wallstreet.domain.repository.TradeRepository
import com.wallstreet.domain.usecase.trade.toDuration
import com.wallstreet.presentation.home.TimePeriod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.ZoneId

class GetStrategyStatsUsecase(
    private val strategyRepository: StrategyRepository,
    private val tradeRepository: TradeRepository
    ) {
    operator fun invoke(
        userId: String,
        period: TimePeriod
    ): Flow<List<StrategyStats>> = combine(
        strategyRepository.getStrategies(),
        tradeRepository.getRecentTrades(
            userId,
            LocalDate.now()
            .minus(period.toDuration())
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli(),
            500)
    ) { strategies, trades ->
        val tradesByStrategy = trades.filter { !it.strategyId.isNullOrBlank() }.groupBy { it.strategyId }

        strategies.mapNotNull { strategy ->
            val tradesForStrategy = tradesByStrategy[strategy.id] ?: return@mapNotNull null


            computeStatsForStrategy(strategy, tradesForStrategy, period);
        }

    }

    private fun computeStatsForStrategy(strategy: Strategy, trades: List<Trade>, period: TimePeriod): StrategyStats {
        if(trades.isEmpty()){
            return StrategyStats(
                strategy = strategy,
                totalTrades = 0,
                totalPnl = 0.0,
                winRate = 0.0,
                rrRatio = 0.0,
                avgProfitPerTrade = 0.0,
                period = period
            )
        }
        val totalPnl = trades.sumOf { it.profitLoss ?: 0.0 }
        val wins = trades.filter { (it.profitLoss ?: 0.0) > 0 }
        val losses = trades.filter { (it.profitLoss ?: 0.0) < 0 }
        val winRate = wins.size.toDouble() / trades.size * 100
        val avgWin = if (wins.isEmpty()) 0.0 else wins.sumOf { it.profitLoss ?: 0.0 } / wins.size
        val avgLoss = if (losses.isEmpty()) 0.0 else losses.sumOf { -(it.profitLoss ?: 0.0) } / losses.size
        val rrRatio = if (avgLoss > 0) avgWin / avgLoss else 0.0

        return StrategyStats(
            strategy = strategy,
            totalTrades = trades.size,
            totalPnl = totalPnl,
            winRate = winRate,
            rrRatio = rrRatio,
            avgProfitPerTrade = totalPnl / trades.size,
            period = period
        )
    }
}