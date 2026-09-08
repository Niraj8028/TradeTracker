package com.wallstreet.domain.insights.detectors

import com.wallstreet.domain.insights.StrategyInsightDetector
import com.wallstreet.domain.insights.copy.InsightThresholds as T
import com.wallstreet.domain.model.insights.Insight
import com.wallstreet.domain.model.insights.InsightCategory.STRATEGY
import com.wallstreet.domain.model.insights.InsightSeverity.CRITICAL
import com.wallstreet.domain.model.insights.InsightSeverity.INFO
import com.wallstreet.domain.model.insights.InsightSeverity.POSITIVE
import com.wallstreet.domain.model.insights.InsightSeverity.WARNING
import com.wallstreet.domain.model.insights.StrategyInsightContext
import com.wallstreet.domain.model.insights.StrategyVerdict
import kotlin.math.abs

private fun pf1(pf: Double?): String = pf?.let { String.format("%.1f", it) } ?: "—"

/** The single best strategy worth scaling up. */
internal object ScaleUpStrategyDetector : StrategyInsightDetector {
    override val id = "strategy.scaleUp"
    override fun detect(ctx: StrategyInsightContext): List<Insight> {
        val best = ctx.perStrategy.map { it.first }
            .filter {
                it.stats.totalTrades >= T.MIN_SAMPLE_STRATEGY &&
                    it.stats.totalPnl > 0 &&
                    (it.profitFactor ?: 0.0) >= T.STRATEGY_SCALE_PF
            }
            .maxByOrNull { it.expectancy } ?: return emptyList()
        return listOf(
            insight(
                id = "$id:${best.strategy.id}", category = STRATEGY, severity = POSITIVE,
                title = best.strategy.name, templateKey = "strategy.scaleUp",
                args = mapOf(
                    "strategy" to best.strategy.name,
                    "pf" to pf1(best.profitFactor),
                    "trades" to best.stats.totalTrades.toString(),
                    "pnl" to money(best.stats.totalPnl, ctx.currencySymbol),
                ),
                impact = best.stats.totalPnl, sample = best.stats.totalTrades,
                strategyId = best.strategy.id, verdict = StrategyVerdict.SCALE_UP,
            )
        )
    }
}

/** A strategy that is losing money with a sub-1 profit factor. */
internal object DropStrategyDetector : StrategyInsightDetector {
    override val id = "strategy.drop"
    override fun detect(ctx: StrategyInsightContext): List<Insight> =
        ctx.perStrategy.map { it.first }
            .filter {
                it.stats.totalTrades >= T.MIN_SAMPLE_STRATEGY &&
                    it.stats.totalPnl < 0 &&
                    (it.profitFactor ?: Double.MAX_VALUE) < T.PROFIT_FACTOR_FLOOR
            }
            .map { s ->
                val severity = if (abs(s.stats.totalPnl) >= 3 * T.IMPACT_SCALE) CRITICAL else WARNING
                insight(
                    id = "$id:${s.strategy.id}", category = STRATEGY, severity = severity,
                    title = s.strategy.name, templateKey = "strategy.drop",
                    args = mapOf(
                        "strategy" to s.strategy.name,
                        "pnl" to money(s.stats.totalPnl, ctx.currencySymbol),
                        "trades" to s.stats.totalTrades.toString(),
                        "pf" to pf1(s.profitFactor),
                    ),
                    impact = s.stats.totalPnl, sample = s.stats.totalTrades,
                    strategyId = s.strategy.id, verdict = StrategyVerdict.DROP,
                )
            }
}

/** A strategy whose win rate dropped, or flipped from green to red. */
internal object DecliningStrategyDetector : StrategyInsightDetector {
    override val id = "strategy.declining"
    override fun detect(ctx: StrategyInsightContext): List<Insight> =
        ctx.perStrategy.mapNotNull { (cur, prev) ->
            if (prev == null) return@mapNotNull null
            if (cur.stats.totalTrades < 5 || prev.stats.totalTrades < 5) return@mapNotNull null
            val wrDrop = prev.stats.winRate - cur.stats.winRate
            val flipped = prev.stats.totalPnl >= 0 && cur.stats.totalPnl < 0
            if (wrDrop < T.WIN_RATE_GAP_PP && !flipped) return@mapNotNull null
            insight(
                id = "$id:${cur.strategy.id}", category = STRATEGY, severity = WARNING,
                title = cur.strategy.name, templateKey = "strategy.declining",
                args = mapOf(
                    "strategy" to cur.strategy.name,
                    "prevWr" to percent(prev.stats.winRate),
                    "curWr" to percent(cur.stats.winRate),
                ),
                impact = cur.stats.totalPnl, sample = cur.stats.totalTrades,
                deltaBoost = T.DELTA_BOOST,
                strategyId = cur.strategy.id, verdict = StrategyVerdict.REVIEW,
            )
        }
}

/** A strategy whose win rate is climbing while it's profitable. */
internal object ImprovingStrategyDetector : StrategyInsightDetector {
    override val id = "strategy.improving"
    override fun detect(ctx: StrategyInsightContext): List<Insight> =
        ctx.perStrategy.mapNotNull { (cur, prev) ->
            if (prev == null) return@mapNotNull null
            if (cur.stats.totalTrades < 5 || prev.stats.totalTrades < 5) return@mapNotNull null
            if (cur.stats.winRate - prev.stats.winRate < T.WIN_RATE_GAP_PP) return@mapNotNull null
            if (cur.stats.totalPnl <= 0) return@mapNotNull null
            insight(
                id = "$id:${cur.strategy.id}", category = STRATEGY, severity = POSITIVE,
                title = cur.strategy.name, templateKey = "strategy.improving",
                args = mapOf(
                    "strategy" to cur.strategy.name,
                    "prevWr" to percent(prev.stats.winRate),
                    "curWr" to percent(cur.stats.winRate),
                    "pnl" to money(cur.stats.totalPnl, ctx.currencySymbol),
                ),
                impact = cur.stats.totalPnl, sample = cur.stats.totalTrades,
                deltaBoost = T.DELTA_BOOST,
                strategyId = cur.strategy.id, verdict = StrategyVerdict.KEEP,
            )
        }
}

/** Not enough trades yet to judge a strategy. */
internal object NeedsDataStrategyDetector : StrategyInsightDetector {
    override val id = "strategy.needsData"
    override fun detect(ctx: StrategyInsightContext): List<Insight> =
        ctx.perStrategy.map { it.first }
            .filter { it.stats.totalTrades in 1 until T.MIN_SAMPLE_STRATEGY }
            .map { s ->
                insight(
                    id = "$id:${s.strategy.id}", category = STRATEGY, severity = INFO,
                    title = s.strategy.name, templateKey = "strategy.needsData",
                    args = mapOf(
                        "trades" to s.stats.totalTrades.toString(),
                        "strategy" to s.strategy.name,
                        "min" to T.MIN_SAMPLE_STRATEGY.toString(),
                    ),
                    impact = 0.0, sample = s.stats.totalTrades,
                    strategyId = s.strategy.id, verdict = StrategyVerdict.NEEDS_MORE_DATA,
                )
            }
}

/** A profitable strategy with a shallow drawdown — keep running it. */
internal object SteadyStrategyDetector : StrategyInsightDetector {
    override val id = "strategy.steady"
    override fun detect(ctx: StrategyInsightContext): List<Insight> =
        ctx.perStrategy.map { it.first }
            .filter { s ->
                s.stats.totalTrades >= T.MIN_SAMPLE_STRATEGY &&
                    s.stats.totalPnl > 0 &&
                    (s.profitFactor ?: 0.0) >= T.STRATEGY_STEADY_PF &&
                    s.maxDrawdown <= T.STRATEGY_STEADY_DD_FRACTION * s.stats.totalPnl
            }
            .map { s ->
                insight(
                    id = "$id:${s.strategy.id}", category = STRATEGY, severity = POSITIVE,
                    title = s.strategy.name, templateKey = "strategy.steady",
                    args = mapOf(
                        "strategy" to s.strategy.name,
                        "dd" to money(s.maxDrawdown, ctx.currencySymbol),
                        "pnl" to money(s.stats.totalPnl, ctx.currencySymbol),
                    ),
                    impact = s.stats.totalPnl, sample = s.stats.totalTrades,
                    strategyId = s.strategy.id, verdict = StrategyVerdict.KEEP,
                )
            }
}
