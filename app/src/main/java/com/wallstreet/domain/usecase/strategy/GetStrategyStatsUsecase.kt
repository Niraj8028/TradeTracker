package com.wallstreet.domain.usecase.strategy

import com.wallstreet.domain.model.TimePeriod
import com.wallstreet.domain.model.strategy.StrategyStats
import com.wallstreet.domain.model.toDuration
import com.wallstreet.domain.repository.StrategyRepository
import com.wallstreet.domain.repository.TradeRepository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.ZoneId

class GetStrategyStatsUsecase(
    private val strategyRepository: StrategyRepository,
    private val tradeRepository: TradeRepository,
    private val calculator: StrategyStatsCalculator,
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
            500
        )
    ) { strategies, trades ->
        val tradesByStrategy =
            trades.filter { !it.strategyId.isNullOrBlank() }.groupBy { it.strategyId }

        strategies.map { strategy ->
            val tradesForStrategy = tradesByStrategy[strategy.id] ?: emptyList()
            calculator.compute(strategy, tradesForStrategy, period)
        }
    }
}
