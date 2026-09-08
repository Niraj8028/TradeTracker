package com.wallstreet.domain.insights.detectors

import com.wallstreet.core.util.toDayOfWeek
import com.wallstreet.domain.insights.InsightDetector
import com.wallstreet.domain.insights.copy.InsightThresholds as T
import com.wallstreet.domain.model.insights.Insight
import com.wallstreet.domain.model.insights.InsightCategory.DISCIPLINE
import com.wallstreet.domain.model.insights.InsightCategory.RISK
import com.wallstreet.domain.model.insights.InsightCategory.TIMING
import com.wallstreet.domain.model.insights.InsightContext
import com.wallstreet.domain.model.insights.InsightSeverity.CRITICAL
import com.wallstreet.domain.model.insights.InsightSeverity.POSITIVE
import com.wallstreet.domain.model.insights.InsightSeverity.WARNING
import kotlin.math.abs
import kotlin.math.max

private data class DayAgg(val pnl: Double, val count: Int)

private fun closedByDay(ctx: InsightContext): Map<java.time.DayOfWeek, DayAgg> =
    ctx.current.trades
        .filter { it.profitLoss != null }
        .groupBy { it.toDayOfWeek() }
        .mapValues { (_, ts) -> DayAgg(ts.sumOf { it.profitLoss ?: 0.0 }, ts.size) }

/** Your strongest trading day. */
internal object BestDayDetector : InsightDetector {
    override val id = "timing.bestDay"
    override val category = TIMING
    override fun detect(ctx: InsightContext): List<Insight> {
        val best = closedByDay(ctx).entries.maxByOrNull { it.value.pnl } ?: return emptyList()
        val (day, agg) = best
        if (agg.pnl < T.IMPACT_SCALE || agg.count < T.MIN_SAMPLE_DAY) return emptyList()
        return listOf(
            insight(
                id = id, category = TIMING, severity = POSITIVE, title = dayLabel(day),
                templateKey = "timing.bestDay",
                args = mapOf("day" to dayLabel(day), "pnl" to money(agg.pnl, ctx.currencySymbol)),
                impact = agg.pnl, sample = agg.count,
                deltaBoost = roleBoost(ctx.roles, "Scalping", "Intraday"),
            )
        )
    }
}

/** A day of the week that consistently loses. */
internal object WorstDayDetector : InsightDetector {
    override val id = "timing.worstDay"
    override val category = TIMING
    override fun detect(ctx: InsightContext): List<Insight> {
        val worst = closedByDay(ctx).entries
            .filter { it.value.pnl < 0 && abs(it.value.pnl) >= T.IMPACT_SCALE && it.value.count >= T.MIN_SAMPLE_DAY }
            .minByOrNull { it.value.pnl } ?: return emptyList()
        val (day, agg) = worst
        return listOf(
            insight(
                id = id, category = TIMING, severity = WARNING, title = dayLabel(day),
                templateKey = "timing.worstDay",
                args = mapOf(
                    "day" to dayLabel(day),
                    "pnl" to money(agg.pnl, ctx.currencySymbol),
                    "trades" to agg.count.toString(),
                ),
                impact = agg.pnl, sample = agg.count,
                deltaBoost = roleBoost(ctx.roles, "Scalping", "Intraday"),
            )
        )
    }
}

/** Losers outweigh winners overall. */
internal object ProfitFactorRiskDetector : InsightDetector {
    override val id = "risk.profitFactor"
    override val category = RISK
    override fun detect(ctx: InsightContext): List<Insight> {
        val pf = ctx.current.overview.profitFactor ?: return emptyList()
        if (pf >= T.PROFIT_FACTOR_FLOOR || ctx.current.closedCount < 10) return emptyList()
        return listOf(
            insight(
                id = id, category = RISK, severity = CRITICAL, title = "Profit factor below 1",
                templateKey = "risk.profitFactor",
                args = mapOf("pf" to String.format("%.1f", pf)),
                impact = abs(ctx.current.overview.netPnl), sample = ctx.current.closedCount,
            )
        )
    }
}

/** A deep equity drawdown relative to net P&L. */
internal object DrawdownRiskDetector : InsightDetector {
    override val id = "risk.drawdown"
    override val category = RISK
    override fun detect(ctx: InsightContext): List<Insight> {
        if (ctx.current.closedCount < 10) return emptyList()
        val dd = ctx.current.maxDrawdown.amount
        val floor = max(T.IMPACT_SCALE, 0.5 * abs(ctx.current.overview.netPnl))
        if (dd < floor) return emptyList()
        return listOf(
            insight(
                id = id, category = RISK, severity = WARNING, title = "Deep drawdown",
                templateKey = "risk.drawdown",
                args = mapOf("dd" to money(-dd, ctx.currencySymbol)),
                impact = dd, sample = ctx.current.closedCount,
            )
        )
    }
}

/**
 * Size of the average win vs the average loss. Winners much bigger than losers is the goal;
 * near-parity means the edge is thin and the trader should cut losses sooner or hold winners
 * for a bigger target.
 */
internal object RewardRiskDetector : InsightDetector {
    override val id = "risk.rewardRisk"
    override val category = RISK
    override fun detect(ctx: InsightContext): List<Insight> {
        val closed = ctx.current.trades.mapNotNull { it.profitLoss }
        val wins = closed.count { it > 0.0 }
        val losses = closed.count { it < 0.0 }
        if (closed.size < T.MIN_SAMPLE_RR || wins < 3 || losses < 3) return emptyList()

        val avgWin = ctx.current.avgWin
        val avgLoss = ctx.current.avgLoss // positive magnitude
        if (avgWin <= 0.0 || avgLoss <= 0.0) return emptyList()

        val rr = avgWin / avgLoss
        val args = mapOf(
            "rr" to "${String.format("%.1f", rr)}x",
            "avgWin" to money(avgWin, ctx.currencySymbol),
            "avgLoss" to money(-avgLoss, ctx.currencySymbol),
        )
        return when {
            rr < 1.0 -> listOf(
                insight(id, RISK, CRITICAL, "Loss size", "risk.rewardRisk.inverted", args,
                    impact = avgLoss, sample = closed.size)
            )
            rr < T.RR_WEAK_MAX -> listOf(
                insight(id, RISK, WARNING, "Reward vs risk", "risk.rewardRisk.weak", args,
                    impact = avgLoss, sample = closed.size)
            )
            rr >= T.RR_STRONG -> listOf(
                insight(id, RISK, POSITIVE, "Reward vs risk", "risk.rewardRisk.strong", args,
                    impact = 0.0, sample = closed.size)
            )
            else -> emptyList()
        }
    }
}

/** Currently on a losing streak. */
internal object LossStreakDetector : InsightDetector {
    override val id = "discipline.lossStreak"
    override val category = DISCIPLINE
    override fun detect(ctx: InsightContext): List<Insight> {
        val streak = ctx.current.currentStreak
        if (streak > -T.LOSS_STREAK) return emptyList()
        val n = abs(streak)
        return listOf(
            insight(
                id = id, category = DISCIPLINE, severity = WARNING, title = "Losing streak",
                templateKey = "discipline.lossStreak",
                args = mapOf("n" to n.toString()),
                impact = 0.0, sample = n,
                deltaBoost = roleBoost(ctx.roles, "Scalping", "Intraday"),
            )
        )
    }
}
