package com.wallstreet.domain.insights.detectors

import com.wallstreet.domain.insights.InsightDetector
import com.wallstreet.domain.insights.copy.InsightThresholds as T
import com.wallstreet.domain.model.insights.Insight
import com.wallstreet.domain.model.insights.InsightCategory.DIRECTION
import com.wallstreet.domain.model.insights.InsightCategory.TREND
import com.wallstreet.domain.model.insights.InsightContext
import com.wallstreet.domain.model.insights.InsightSeverity.INFO
import com.wallstreet.domain.model.insights.InsightSeverity.WARNING
import kotlin.math.abs

/** Long vs short win-rate edge. */
internal object DirectionalEdgeDetector : InsightDetector {
    override val id = "direction.edge"
    override val category = DIRECTION
    override fun detect(ctx: InsightContext): List<Insight> {
        val l = ctx.current.summary.long
        val s = ctx.current.summary.short
        if (l.count < T.MIN_SAMPLE_DIRECTION || s.count < T.MIN_SAMPLE_DIRECTION) return emptyList()
        if (abs(l.winRate - s.winRate) < T.WIN_RATE_GAP_PP) return emptyList()
        val longBetter = l.winRate >= s.winRate
        return listOf(
            insight(
                id = id, category = DIRECTION, severity = INFO, title = "Directional edge",
                templateKey = "direction.edge",
                args = mapOf(
                    "betterWr" to percent(if (longBetter) l.winRate else s.winRate),
                    "betterSide" to if (longBetter) "long" else "short",
                    "worseWr" to percent(if (longBetter) s.winRate else l.winRate),
                ),
                impact = 0.0, sample = l.count + s.count,
            )
        )
    }
}

/** One side is net negative while the other is green. */
internal object BleedingDirectionDetector : InsightDetector {
    override val id = "direction.bleeding"
    override val category = DIRECTION
    override fun detect(ctx: InsightContext): List<Insight> {
        val l = ctx.current.summary.long
        val s = ctx.current.summary.short
        val longBleeds = l.pnl < 0 && s.pnl >= 0
        val shortBleeds = s.pnl < 0 && l.pnl >= 0
        if (!longBleeds && !shortBleeds) return emptyList()
        val loser = if (longBleeds) l else s
        if (loser.count < 3) return emptyList()
        return listOf(
            insight(
                id = id, category = DIRECTION, severity = WARNING, title = "One side is bleeding",
                templateKey = "direction.bleeding",
                args = mapOf(
                    "loserSide" to if (longBleeds) "long" else "short",
                    "loserPnl" to money(loser.pnl, ctx.currencySymbol),
                    "winnerSide" to if (longBleeds) "short" else "long",
                ),
                impact = loser.pnl, sample = loser.count,
            )
        )
    }
}

/** You perform far better in one market condition than another. */
internal object TrendEdgeDetector : InsightDetector {
    override val id = "trend.edge"
    override val category = TREND
    override fun detect(ctx: InsightContext): List<Insight> {
        val stats = ctx.current.trend.stats.filter { it.trades > 0 }
        val best = stats.maxByOrNull { it.winRate } ?: return emptyList()
        val worst = stats.minByOrNull { it.winRate } ?: return emptyList()
        if (best.direction == worst.direction) return emptyList()
        if (best.trades < T.MIN_SAMPLE_DIRECTION) return emptyList()
        if (best.winRate - worst.winRate < T.WIN_RATE_GAP_PP) return emptyList()
        return listOf(
            insight(
                id = id, category = TREND, severity = INFO, title = "Market-condition edge",
                templateKey = "trend.edge",
                args = mapOf(
                    "bestLabel" to trendLabel(best.direction),
                    "bestWr" to percent(best.winRate),
                    "worstWr" to percent(worst.winRate),
                    "worstLabel" to trendLabel(worst.direction),
                ),
                impact = 0.0, sample = best.trades,
                deltaBoost = roleBoost(ctx.roles, "Swing"),
            )
        )
    }
}

/** A market condition that is costing you money. */
internal object TrendLosingDetector : InsightDetector {
    override val id = "trend.losing"
    override val category = TREND
    override fun detect(ctx: InsightContext): List<Insight> =
        ctx.current.trend.stats
            .filter { it.trades >= T.MIN_SAMPLE_DAY && (it.totalPnl < 0 || it.winRate < T.TREND_WEAK_WR) }
            .map { s ->
                insight(
                    id = "$id:${s.direction}", category = TREND, severity = WARNING,
                    title = trendLabel(s.direction), templateKey = "trend.losing",
                    args = mapOf(
                        "label" to trendLabel(s.direction),
                        "pnl" to money(s.totalPnl, ctx.currencySymbol),
                    ),
                    impact = s.totalPnl, sample = s.trades,
                    deltaBoost = roleBoost(ctx.roles, "Swing"),
                )
            }
}
