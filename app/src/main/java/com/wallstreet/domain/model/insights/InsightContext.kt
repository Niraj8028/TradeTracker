package com.wallstreet.domain.model.insights

import com.wallstreet.domain.model.TimePeriod

/**
 * The full input to the analytics detectors: current window, the equal-length prior window
 * (`null` when there isn't enough history), per-mistake comparisons, and personalization.
 */
data class InsightContext(
    val current: WindowStats,
    val previous: WindowStats?,
    val mistakeComparisons: List<MistakeComparison>,
    val overallMistakeRate: Double,        // tagged trades / closed trades, 0..1
    val prevOverallMistakeRate: Double?,
    val roles: List<String>,
    val currencySymbol: String,
    val period: TimePeriod,
)

/** Input to the strategy detectors: each strategy's current window paired with its prior one. */
data class StrategyInsightContext(
    val perStrategy: List<Pair<StrategyWindowStats, StrategyWindowStats?>>,
    val roles: List<String>,
    val currencySymbol: String,
    val period: TimePeriod,
)
