package com.wallstreet.domain.insights

import com.wallstreet.domain.model.insights.Insight
import com.wallstreet.domain.model.insights.InsightCategory
import com.wallstreet.domain.model.insights.InsightContext
import com.wallstreet.domain.model.insights.StrategyInsightContext

/** A single scored rule over the analytics [InsightContext]. Pure — no Android, no coroutines. */
interface InsightDetector {
    val id: String
    val category: InsightCategory
    fun detect(ctx: InsightContext): List<Insight>
}

/** A single scored rule over the [StrategyInsightContext] (one insight per strategy). */
interface StrategyInsightDetector {
    val id: String
    fun detect(ctx: StrategyInsightContext): List<Insight>
}
