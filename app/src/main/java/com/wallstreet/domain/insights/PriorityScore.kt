package com.wallstreet.domain.insights

import com.wallstreet.domain.insights.copy.InsightThresholds
import com.wallstreet.domain.model.insights.InsightSeverity
import kotlin.math.abs
import kotlin.math.min

/**
 * Blends financial impact, severity and sample-size confidence into a 0..1 priority.
 * See the design doc for the formula.
 */
object PriorityScore {

    fun of(
        financialImpact: Double,
        severity: InsightSeverity,
        sampleSize: Int,
        deltaBoost: Double = 0.0,
    ): Double {
        val impact = abs(financialImpact)
        val impactScore = impact / (impact + InsightThresholds.IMPACT_SCALE)
        val severityWeight = when (severity) {
            InsightSeverity.CRITICAL -> 1.0
            InsightSeverity.WARNING -> 0.66
            InsightSeverity.POSITIVE -> 0.45
            InsightSeverity.INFO -> 0.33
        }
        val confidence = min(1.0, sampleSize.toDouble() / InsightThresholds.CONFIDENT_SAMPLE)
        val raw = InsightThresholds.W_IMPACT * impactScore +
            InsightThresholds.W_SEVERITY * severityWeight +
            InsightThresholds.W_CONFIDENCE * confidence +
            deltaBoost
        return raw.coerceIn(0.0, 1.0)
    }
}
