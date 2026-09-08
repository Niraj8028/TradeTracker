package com.wallstreet.testutil

import com.wallstreet.core.util.TradeMath
import com.wallstreet.domain.model.Strategy
import com.wallstreet.domain.model.TimePeriod
import com.wallstreet.domain.model.Trade
import com.wallstreet.domain.model.insights.StrategyWindowStats
import com.wallstreet.domain.usecase.strategy.StrategyStatsCalculator

/** Builds a [StrategyWindowStats] from a bare trade list, mirroring the production wiring. */
fun strategyWindow(
    name: String,
    trades: List<Trade>,
    id: String = name,
    period: TimePeriod = TimePeriod.ONE_MONTH,
): StrategyWindowStats {
    val strategy = Strategy(id = id, name = name)
    return StrategyWindowStats(
        strategy = strategy,
        stats = StrategyStatsCalculator().compute(strategy, trades, period),
        profitFactor = TradeMath.profitFactor(trades),
        expectancy = TradeMath.expectancy(trades),
        maxDrawdown = TradeMath.maxDrawdown(trades).amount,
        winStreak = TradeMath.winStreak(trades),
        lossStreak = TradeMath.lossStreak(trades),
        closedCount = trades.count { it.profitLoss != null },
    )
}
