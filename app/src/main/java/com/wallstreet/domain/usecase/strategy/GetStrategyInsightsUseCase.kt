package com.wallstreet.domain.usecase.strategy

import com.wallstreet.core.util.TradeMath
import com.wallstreet.domain.insights.InsightEngine
import com.wallstreet.domain.insights.copy.InsightThresholds
import com.wallstreet.domain.model.Strategy
import com.wallstreet.domain.model.TimePeriod
import com.wallstreet.domain.model.Trade
import com.wallstreet.domain.model.insights.Insight
import com.wallstreet.domain.model.insights.InsightCategory
import com.wallstreet.domain.model.insights.StrategyInsightContext
import com.wallstreet.domain.model.insights.StrategyVerdict
import com.wallstreet.domain.model.insights.StrategyWindowStats
import com.wallstreet.domain.model.toDuration
import com.wallstreet.domain.repository.StrategyRepository
import com.wallstreet.domain.repository.TradeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate
import java.time.ZoneId

data class StrategyInsightsResult(
    val headline: Insight?,
    val verdictByStrategyId: Map<String, StrategyVerdict>,
    val insightsByStrategyId: Map<String, List<Insight>>,
) {
    companion object {
        val EMPTY = StrategyInsightsResult(null, emptyMap(), emptyMap())
    }
}

/**
 * Groups the user's trades by strategy, splits each into the current window and the equal
 * prior window, and runs the strategy detectors. Queries the trade repo directly (limit 1000)
 * from the previous-window start so both windows come from one read.
 */
class GetStrategyInsightsUseCase(
    private val strategyRepository: StrategyRepository,
    private val tradeRepository: TradeRepository,
    private val insightEngine: InsightEngine,
    private val statsCalculator: StrategyStatsCalculator,
) {

    operator fun invoke(
        userId: String,
        period: TimePeriod,
        roles: List<String>,
        currencySymbol: String,
        now: LocalDate = LocalDate.now(),
    ): Flow<StrategyInsightsResult> {
        if (userId.isBlank()) return flowOf(StrategyInsightsResult.EMPTY)

        val zone = ZoneId.systemDefault()
        val nowMs = now.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val windowed = period != TimePeriod.ALL
        val curStartMs = if (windowed) {
            now.minus(period.toDuration()).atStartOfDay(zone).toInstant().toEpochMilli()
        } else 0L
        val prevStartMs = if (windowed) {
            now.minus(period.toDuration()).minus(period.toDuration())
                .atStartOfDay(zone).toInstant().toEpochMilli()
        } else 0L

        return combine(
            strategyRepository.getStrategies(),
            tradeRepository.getRecentTrades(userId, prevStartMs, 1000),
        ) { strategies, trades ->
            val byStrategy = trades.filter { !it.strategyId.isNullOrBlank() }.groupBy { it.strategyId }

            val pairs: List<Pair<StrategyWindowStats, StrategyWindowStats?>> = strategies.map { strategy ->
                val all = byStrategy[strategy.id].orEmpty()
                val currentTrades =
                    if (windowed) all.filter { it.tradeDate in curStartMs until nowMs } else all
                val previousTrades =
                    if (windowed) all.filter { it.tradeDate in prevStartMs until curStartMs } else emptyList()

                val cur = window(strategy, currentTrades, period)
                val prev = if (windowed &&
                    previousTrades.size >= InsightThresholds.MIN_PREV_WINDOW_TRADES
                ) window(strategy, previousTrades, period) else null
                cur to prev
            }

            val ctx = StrategyInsightContext(pairs, roles, currencySymbol, period)
            val result = insightEngine.runStrategy(ctx)

            val byId = result.all
                .filter { it.strategyId != null }
                .groupBy { it.strategyId!! }

            val verdicts = pairs.mapNotNull { (cur, _) ->
                if (cur.stats.totalTrades == 0) return@mapNotNull null
                val top = byId[cur.strategy.id]?.maxByOrNull { it.priority }
                val verdict = top?.verdict
                    ?: if (cur.stats.totalTrades < InsightThresholds.MIN_SAMPLE_STRATEGY) {
                        StrategyVerdict.NEEDS_MORE_DATA
                    } else {
                        StrategyVerdict.KEEP
                    }
                cur.strategy.id to verdict
            }.toMap()

            StrategyInsightsResult(
                headline = result.all.firstOrNull { it.category == InsightCategory.STRATEGY },
                verdictByStrategyId = verdicts,
                insightsByStrategyId = byId,
            )
        }
    }

    private fun window(strategy: Strategy, trades: List<Trade>, period: TimePeriod) =
        StrategyWindowStats(
            strategy = strategy,
            stats = statsCalculator.compute(strategy, trades, period),
            profitFactor = TradeMath.profitFactor(trades),
            expectancy = TradeMath.expectancy(trades),
            maxDrawdown = TradeMath.maxDrawdown(trades).amount,
            winStreak = TradeMath.winStreak(trades),
            lossStreak = TradeMath.lossStreak(trades),
            closedCount = trades.count { it.profitLoss != null },
        )
}
