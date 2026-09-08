package com.wallstreet.domain.insights.detectors

import com.wallstreet.domain.insights.InsightDetector
import com.wallstreet.domain.insights.StrategyInsightDetector

/** The analytics detectors run over an [com.wallstreet.domain.model.insights.InsightContext]. */
val analyticsDetectors: List<InsightDetector> = listOf(
    // Mistakes
    CostliestMistakeDetector,
    OverallMistakeRateDetector,
    RepeatingMistakeDetector,
    ReducingMistakeDetector,
    ClearedMistakeDetector,
    NewMistakeDetector,
    CleanVsTaggedEdgeDetector,
    MistakeConcentrationDetector,
    DoNotEnterMistakeDetector,
    // Direction / trend
    DirectionalEdgeDetector,
    BleedingDirectionDetector,
    TrendEdgeDetector,
    TrendLosingDetector,
    // Timing / risk / discipline
    BestDayDetector,
    WorstDayDetector,
    ProfitFactorRiskDetector,
    RewardRiskDetector,
    DrawdownRiskDetector,
    LossStreakDetector,
)

/** The strategy detectors run over a [com.wallstreet.domain.model.insights.StrategyInsightContext]. */
val strategyDetectors: List<StrategyInsightDetector> = listOf(
    ScaleUpStrategyDetector,
    DropStrategyDetector,
    DecliningStrategyDetector,
    ImprovingStrategyDetector,
    NeedsDataStrategyDetector,
    SteadyStrategyDetector,
)
