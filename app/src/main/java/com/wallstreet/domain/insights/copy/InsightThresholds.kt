package com.wallstreet.domain.insights.copy

/**
 * Every tunable constant the engine uses. Kept in one object so it can later be backed by a
 * remote source without touching detector code.
 */
object InsightThresholds {

    // Minimum trade counts before a detector is allowed to fire.
    const val MIN_SAMPLE_MISTAKE = 3
    const val MIN_SAMPLE_STRATEGY = 8
    const val MIN_SAMPLE_DIRECTION = 5
    const val MIN_SAMPLE_DAY = 3
    const val MIN_PREV_WINDOW_TRADES = 3
    const val CONFIDENT_SAMPLE = 10

    // Trigger thresholds.
    const val WIN_RATE_GAP_PP = 10.0
    const val CLEAN_EDGE_GAP_PP = 15.0
    const val TREND_WEAK_WR = 45.0
    const val DO_NOT_ENTER_WR = 30.0
    const val PROFIT_FACTOR_FLOOR = 1.0
    const val LOSS_STREAK = 3
    const val REDUCE_FRACTION = 0.34          // ≥ 1/3 fewer occurrences ⇒ "reducing"
    const val CONCENTRATION_PCT = 50.0
    const val OVERALL_RATE_MOVE_PP = 10.0
    const val STRATEGY_SCALE_PF = 1.5
    const val STRATEGY_STEADY_PF = 1.2
    const val STRATEGY_STEADY_DD_FRACTION = 0.4

    // Priority scoring.
    const val IMPACT_SCALE = 500.0            // currency units
    const val W_IMPACT = 0.50
    const val W_SEVERITY = 0.30
    const val W_CONFIDENCE = 0.20
    const val DELTA_BOOST = 0.10
    const val ROLE_BOOST = 0.05
}
