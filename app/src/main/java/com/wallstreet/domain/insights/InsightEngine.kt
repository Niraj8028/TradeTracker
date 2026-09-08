package com.wallstreet.domain.insights

import com.wallstreet.domain.analytics.AnalyticsManager
import com.wallstreet.domain.model.insights.Insight
import com.wallstreet.domain.model.insights.InsightCategory
import com.wallstreet.domain.model.insights.InsightContext
import com.wallstreet.domain.model.insights.StrategyInsightContext

data class InsightEngineConfig(
    val minPriority: Double = 0.15,
    val perCategoryCap: Int = 4,
    val globalCap: Int = 12,
)

data class InsightResult(
    val all: List<Insight>,
    val byCategory: Map<InsightCategory, List<Insight>>,
)

/**
 * Runs every detector, swallows individual failures, then dedupes by id (keeping the
 * highest-priority duplicate), drops low-priority insights, sorts, and caps per category and
 * overall. Pure apart from the optional [analytics] error sink.
 */
class InsightEngine(
    private val detectors: List<InsightDetector>,
    private val strategyDetectors: List<StrategyInsightDetector> = emptyList(),
    private val config: InsightEngineConfig = InsightEngineConfig(),
    private val analytics: AnalyticsManager? = null,
) {

    fun run(ctx: InsightContext): InsightResult =
        assemble(detectors.flatMap { d -> safeDetect(d.id) { d.detect(ctx) } })

    fun runStrategy(ctx: StrategyInsightContext): InsightResult =
        assemble(strategyDetectors.flatMap { d -> safeDetect(d.id) { d.detect(ctx) } })

    private fun safeDetect(id: String, block: () -> List<Insight>): List<Insight> =
        try {
            block()
        } catch (e: Exception) {
            analytics?.logError("insight detector '$id' failed", e)
            emptyList()
        }

    private fun assemble(raw: List<Insight>): InsightResult {
        val deduped = raw.groupBy { it.id }.values.map { group -> group.maxBy { it.priority } }
        val ranked = deduped
            .filter { it.priority >= config.minPriority }
            .sortedWith(compareByDescending<Insight> { it.priority }.thenBy { it.severity.ordinal })
        val byCategory = ranked.groupBy { it.category }
            .mapValues { (_, list) -> list.take(config.perCategoryCap) }
        val all = byCategory.values.flatten()
            .sortedByDescending { it.priority }
            .take(config.globalCap)
        return InsightResult(all = all, byCategory = byCategory)
    }
}
