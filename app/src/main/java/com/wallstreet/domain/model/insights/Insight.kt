package com.wallstreet.domain.model.insights

/** Which surface an insight belongs to. */
enum class InsightCategory { MISTAKES, STRATEGY, DIRECTION, TIMING, RISK, DISCIPLINE }

/** Drives the accent colour in the shared InsightsCard. */
enum class InsightSeverity { CRITICAL, WARNING, INFO, POSITIVE }

/** Recommendation for a strategy, shown as a badge on the Strategy screens. */
enum class StrategyVerdict { SCALE_UP, KEEP, REVIEW, DROP, NEEDS_MORE_DATA }

/** Optional labelled metric shown next to an insight body. */
data class MetricChip(
    val label: String,
    val value: String,
    val tone: InsightSeverity = InsightSeverity.INFO,
)

/**
 * A single, ranked, fully-rendered coaching line. `body` already has every placeholder
 * substituted and every currency figure formatted — the UI just renders it.
 */
data class Insight(
    val id: String,                       // stable detector id; per-entity detectors suffix ":<key>"
    val category: InsightCategory,
    val severity: InsightSeverity,
    val title: String,
    val body: String,
    val priority: Double,                  // 0..1
    val chips: List<MetricChip> = emptyList(),
    val evidenceTradeIds: List<String> = emptyList(),
    val strategyId: String? = null,        // STRATEGY insights only
    val verdict: StrategyVerdict? = null,  // STRATEGY insights only
)
