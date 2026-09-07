package com.wallstreet.domain.insights.detectors

import com.wallstreet.domain.insights.InsightDetector
import com.wallstreet.domain.insights.copy.InsightCopy
import com.wallstreet.domain.insights.copy.InsightThresholds as T
import com.wallstreet.domain.model.insights.Insight
import com.wallstreet.domain.model.insights.InsightCategory.MISTAKES
import com.wallstreet.domain.model.insights.InsightContext
import com.wallstreet.domain.model.insights.InsightSeverity.CRITICAL
import com.wallstreet.domain.model.insights.InsightSeverity.INFO
import com.wallstreet.domain.model.insights.InsightSeverity.POSITIVE
import com.wallstreet.domain.model.insights.InsightSeverity.WARNING
import kotlin.math.abs
import kotlin.math.roundToInt

/** The single most expensive mistake this period. */
internal object CostliestMistakeDetector : InsightDetector {
    override val id = "mistake.costliest"
    override val category = MISTAKES
    override fun detect(ctx: InsightContext): List<Insight> {
        val worst = ctx.current.mistakes.topMistakes
            .filter { it.count >= T.MIN_SAMPLE_MISTAKE && it.totalPnlImpact < 0 }
            .minByOrNull { it.totalPnlImpact } ?: return emptyList()
        val severity = if (abs(worst.totalPnlImpact) >= 2 * T.IMPACT_SCALE) CRITICAL else WARNING
        return listOf(
            insight(
                id = id, category = MISTAKES, severity = severity, title = worst.name,
                templateKey = "mistake.costliest",
                args = mapOf(
                    "mistake" to worst.name,
                    "impact" to money(worst.totalPnlImpact, ctx.currencySymbol),
                    "count" to worst.count.toString(),
                    "tradeWord" to tradeWord(worst.count),
                    "advice" to InsightCopy.advice(worst.name),
                ),
                impact = worst.totalPnlImpact, sample = worst.count,
            )
        )
    }
}

/** Are you tagging mistakes more or less often than last period? */
internal object OverallMistakeRateDetector : InsightDetector {
    override val id = "mistake.overallRate"
    override val category = MISTAKES
    override fun detect(ctx: InsightContext): List<Insight> {
        if (ctx.previous == null) return emptyList()
        val prev = ctx.prevOverallMistakeRate ?: return emptyList()
        if (ctx.current.closedCount < 8) return emptyList()
        val movePp = (ctx.overallMistakeRate - prev) * 100.0
        if (abs(movePp) < T.OVERALL_RATE_MOVE_PP) return emptyList()
        val rising = movePp > 0
        return listOf(
            insight(
                id = id, category = MISTAKES,
                severity = if (rising) WARNING else POSITIVE, title = "Mistake rate",
                templateKey = if (rising) "mistake.overallRate.rising" else "mistake.overallRate.falling",
                args = mapOf(
                    "rate" to percent(ctx.overallMistakeRate * 100.0),
                    "prev" to percent(prev * 100.0),
                ),
                impact = 0.0, sample = ctx.current.closedCount, deltaBoost = T.DELTA_BOOST,
            )
        )
    }
}

/** A losing mistake that isn't going away. */
internal object RepeatingMistakeDetector : InsightDetector {
    override val id = "mistake.repeating"
    override val category = MISTAKES
    override fun detect(ctx: InsightContext): List<Insight> {
        if (ctx.previous == null) return emptyList()
        return ctx.mistakeComparisons
            .filter {
                it.prevCount >= 1 && it.count >= it.prevCount &&
                    it.count >= T.MIN_SAMPLE_MISTAKE && it.totalImpact < 0
            }
            .map { c ->
                insight(
                    id = "$id:${c.name}", category = MISTAKES, severity = WARNING, title = c.name,
                    templateKey = "mistake.repeating",
                    args = mapOf(
                        "mistake" to c.name, "count" to c.count.toString(),
                        "prev" to c.prevCount.toString(), "advice" to InsightCopy.advice(c.name),
                    ),
                    impact = c.totalImpact, sample = c.count, deltaBoost = T.DELTA_BOOST,
                )
            }
    }
}

/** A mistake you're doing meaningfully less. */
internal object ReducingMistakeDetector : InsightDetector {
    override val id = "mistake.reducing"
    override val category = MISTAKES
    override fun detect(ctx: InsightContext): List<Insight> {
        if (ctx.previous == null) return emptyList()
        return ctx.mistakeComparisons
            .filter {
                it.prevCount >= T.MIN_SAMPLE_MISTAKE && it.count > 0 &&
                    it.count <= it.prevCount * (1.0 - T.REDUCE_FRACTION)
            }
            .map { c ->
                insight(
                    id = "$id:${c.name}", category = MISTAKES, severity = POSITIVE, title = c.name,
                    templateKey = "mistake.reducing",
                    args = mapOf(
                        "mistake" to c.name, "prev" to c.prevCount.toString(),
                        "count" to c.count.toString(),
                    ),
                    impact = c.prevImpact - c.totalImpact, sample = c.prevCount,
                    deltaBoost = T.DELTA_BOOST,
                )
            }
    }
}

/** A mistake that's gone to zero this period. */
internal object ClearedMistakeDetector : InsightDetector {
    override val id = "mistake.cleared"
    override val category = MISTAKES
    override fun detect(ctx: InsightContext): List<Insight> {
        if (ctx.previous == null) return emptyList()
        return ctx.mistakeComparisons
            .filter { it.isCleared && it.prevCount >= T.MIN_SAMPLE_MISTAKE }
            .map { c ->
                insight(
                    id = "$id:${c.name}", category = MISTAKES, severity = POSITIVE, title = c.name,
                    templateKey = "mistake.cleared",
                    args = mapOf("mistake" to c.name, "prev" to c.prevCount.toString()),
                    impact = c.prevImpact, sample = c.prevCount,
                )
            }
    }
}

/** A mistake that only started this period. */
internal object NewMistakeDetector : InsightDetector {
    override val id = "mistake.new"
    override val category = MISTAKES
    override fun detect(ctx: InsightContext): List<Insight> {
        if (ctx.previous == null) return emptyList()
        return ctx.mistakeComparisons
            .filter { it.isNew && it.count >= 2 }
            .map { c ->
                insight(
                    id = "$id:${c.name}", category = MISTAKES, severity = WARNING, title = c.name,
                    templateKey = "mistake.new",
                    args = mapOf(
                        "mistake" to c.name, "count" to c.count.toString(),
                        "tradeWord" to tradeWord(c.count),
                        "impact" to money(c.totalImpact, ctx.currencySymbol),
                        "advice" to InsightCopy.advice(c.name),
                    ),
                    impact = c.totalImpact, sample = c.count, deltaBoost = T.DELTA_BOOST,
                )
            }
    }
}

/** Clean trades vs mistake-tagged trades — the value of discipline. */
internal object CleanVsTaggedEdgeDetector : InsightDetector {
    override val id = "mistake.cleanEdge"
    override val category = MISTAKES
    override fun detect(ctx: InsightContext): List<Insight> {
        val closed = ctx.current.trades.filter { it.profitLoss != null }
        val clean = closed.filter { it.mistakes.isEmpty() }
        val tagged = closed.filter { it.mistakes.isNotEmpty() }
        if (clean.size < 5 || tagged.isEmpty()) return emptyList()
        val cleanWr = ctx.current.mistakes.cleanTradeWinRate
        val taggedWr = tagged.count { (it.profitLoss ?: 0.0) > 0.0 }.toDouble() / tagged.size * 100.0
        val gap = cleanWr - taggedWr
        if (gap < T.CLEAN_EDGE_GAP_PP) return emptyList()
        return listOf(
            insight(
                id = id, category = MISTAKES, severity = POSITIVE, title = "Discipline pays off",
                templateKey = "mistake.cleanEdge",
                args = mapOf(
                    "cleanWr" to percent(cleanWr), "taggedWr" to percent(taggedWr),
                    "gap" to gap.roundToInt().toString(),
                ),
                impact = 0.0, sample = clean.size,
            )
        )
    }
}

/** One mistake dominating your flagged trades. */
internal object MistakeConcentrationDetector : InsightDetector {
    override val id = "mistake.concentration"
    override val category = MISTAKES
    override fun detect(ctx: InsightContext): List<Insight> {
        val total = ctx.current.mistakes.totalMistakeTrades
        if (total < 4) return emptyList()
        val top = ctx.current.mistakes.topMistakes.maxByOrNull { it.count } ?: return emptyList()
        val share = top.count.toDouble() / total * 100.0
        if (share < T.CONCENTRATION_PCT) return emptyList()
        return listOf(
            insight(
                id = id, category = MISTAKES,
                severity = if (top.totalPnlImpact < 0) WARNING else INFO, title = top.name,
                templateKey = "mistake.concentration",
                args = mapOf("mistake" to top.name, "pct" to percent(share)),
                impact = top.totalPnlImpact, sample = top.count,
            )
        )
    }
}

/** A mistake tag whose trades almost never win. */
internal object DoNotEnterMistakeDetector : InsightDetector {
    override val id = "mistake.doNotEnter"
    override val category = MISTAKES
    override fun detect(ctx: InsightContext): List<Insight> =
        ctx.current.mistakes.topMistakes
            .filter { it.count >= 4 && it.winRate < T.DO_NOT_ENTER_WR }
            .map { s ->
                insight(
                    id = "$id:${s.name}", category = MISTAKES, severity = WARNING, title = s.name,
                    templateKey = "mistake.doNotEnter",
                    args = mapOf("mistake" to s.name, "winRate" to percent(s.winRate)),
                    impact = s.totalPnlImpact, sample = s.count,
                )
            }
}
