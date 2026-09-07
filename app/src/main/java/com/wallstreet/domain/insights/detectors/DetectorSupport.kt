package com.wallstreet.domain.insights.detectors

import com.wallstreet.core.util.formatPnl
import kotlin.math.abs
import kotlin.math.round
import com.wallstreet.domain.insights.PriorityScore
import com.wallstreet.domain.insights.copy.InsightCopy
import com.wallstreet.domain.insights.copy.InsightThresholds
import com.wallstreet.domain.model.TrendDirection
import com.wallstreet.domain.model.insights.Insight
import com.wallstreet.domain.model.insights.InsightCategory
import com.wallstreet.domain.model.insights.InsightSeverity
import com.wallstreet.domain.model.insights.StrategyVerdict
import java.time.DayOfWeek

internal fun money(value: Double, symbol: String): String = value.formatPnl(symbol)

/** [value] is already 0..100. Whole numbers where possible, one decimal otherwise. */
internal fun percent(value: Double): String {
    val rounded = round(value)
    return if (abs(value - rounded) < 0.05) "${rounded.toInt()}%"
    else String.format("%.1f%%", value)
}

internal fun tradeWord(n: Int): String = if (n == 1) "trade" else "trades"

internal fun trendLabel(d: TrendDirection): String = when (d) {
    TrendDirection.UP -> "Uptrend"
    TrendDirection.DOWN -> "Downtrend"
    TrendDirection.SIDEWAYS -> "Sideways"
}

internal fun dayLabel(d: DayOfWeek): String =
    d.name.lowercase().replaceFirstChar { it.uppercase() }

internal fun roleBoost(roles: List<String>, vararg relevant: String): Double =
    if (roles.any { it in relevant }) InsightThresholds.ROLE_BOOST else 0.0

/** Builds a fully-rendered, scored [Insight]. */
internal fun insight(
    id: String,
    category: InsightCategory,
    severity: InsightSeverity,
    title: String,
    templateKey: String,
    args: Map<String, String>,
    impact: Double,
    sample: Int,
    deltaBoost: Double = 0.0,
    strategyId: String? = null,
    verdict: StrategyVerdict? = null,
): Insight = Insight(
    id = id,
    category = category,
    severity = severity,
    title = title,
    body = InsightCopy.render(templateKey, args),
    priority = PriorityScore.of(impact, severity, sample, deltaBoost),
    strategyId = strategyId,
    verdict = verdict,
)
