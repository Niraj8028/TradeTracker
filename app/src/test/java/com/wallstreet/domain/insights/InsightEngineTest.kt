package com.wallstreet.domain.insights

import com.wallstreet.domain.model.TimePeriod
import com.wallstreet.domain.model.insights.Insight
import com.wallstreet.domain.model.insights.InsightCategory
import com.wallstreet.domain.model.insights.InsightSeverity
import com.wallstreet.domain.model.insights.StrategyInsightContext
import org.junit.Assert.assertEquals
import org.junit.Test

/** Exercises the shared assemble pipeline via the (lighter) strategy path. */
class InsightEngineTest {

    private val ctx = StrategyInsightContext(
        perStrategy = emptyList(),
        roles = emptyList(),
        currencySymbol = "$",
        period = TimePeriod.ONE_MONTH,
    )

    private fun insight(
        id: String,
        priority: Double,
        category: InsightCategory = InsightCategory.STRATEGY,
        severity: InsightSeverity = InsightSeverity.INFO,
    ) = Insight(id, category, severity, id, "body of $id", priority)

    private fun detector(vararg out: Insight) = object : StrategyInsightDetector {
        override val id = out.firstOrNull()?.id ?: "empty"
        override fun detect(ctx: StrategyInsightContext) = out.toList()
    }

    private fun engine(
        detectors: List<StrategyInsightDetector>,
        config: InsightEngineConfig = InsightEngineConfig(),
    ) = InsightEngine(emptyList(), detectors, config)

    @Test
    fun `dedupes by id keeping the higher priority`() {
        val result = engine(
            listOf(detector(insight("a", 0.4)), detector(insight("a", 0.9)))
        ).runStrategy(ctx)
        assertEquals(1, result.all.size)
        assertEquals(0.9, result.all.first().priority, 1e-9)
    }

    @Test
    fun `drops insights below minPriority`() {
        val result = engine(
            listOf(detector(insight("keep", 0.5)), detector(insight("drop", 0.05))),
            InsightEngineConfig(minPriority = 0.15),
        ).runStrategy(ctx)
        assertEquals(listOf("keep"), result.all.map { it.id })
    }

    @Test
    fun `caps per category`() {
        val detectors = (1..6).map {
            detector(insight("i$it", 0.9 - it * 0.01, InsightCategory.MISTAKES))
        }
        val result = engine(detectors, InsightEngineConfig(perCategoryCap = 4)).runStrategy(ctx)
        assertEquals(4, result.byCategory.getValue(InsightCategory.MISTAKES).size)
    }

    @Test
    fun `caps globally`() {
        val cats = InsightCategory.entries
        val detectors = (1..20).map { detector(insight("i$it", 0.9, cats[it % cats.size])) }
        val result = engine(
            detectors,
            InsightEngineConfig(globalCap = 5, perCategoryCap = 10),
        ).runStrategy(ctx)
        assertEquals(5, result.all.size)
    }

    @Test
    fun `sorts by priority then severity`() {
        val result = engine(
            listOf(
                detector(insight("low", 0.3)),
                detector(insight("hi-warn", 0.8, severity = InsightSeverity.WARNING)),
                detector(insight("hi-crit", 0.8, severity = InsightSeverity.CRITICAL)),
            )
        ).runStrategy(ctx)
        assertEquals(listOf("hi-crit", "hi-warn", "low"), result.all.map { it.id })
    }

    @Test
    fun `a throwing detector does not stop the others`() {
        val boom = object : StrategyInsightDetector {
            override val id = "boom"
            override fun detect(ctx: StrategyInsightContext): List<Insight> =
                throw IllegalStateException("boom")
        }
        val result = engine(listOf(boom, detector(insight("ok", 0.5)))).runStrategy(ctx)
        assertEquals(listOf("ok"), result.all.map { it.id })
    }
}
