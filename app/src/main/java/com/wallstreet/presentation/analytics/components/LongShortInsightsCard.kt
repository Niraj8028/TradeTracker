package com.wallstreet.presentation.analytics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wallstreet.core.util.formatPercent
import com.wallstreet.core.util.formatPnl
import com.wallstreet.domain.model.TradeStats
import com.wallstreet.domain.model.TradeSummary
import com.wallstreet.ui.theme.DangerRed
import com.wallstreet.ui.theme.PrimaryBlue
import com.wallstreet.ui.theme.SuccessGreen
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * 2–3 plain-English insights about the user's long vs short performance,
 * generated entirely from their own trade data.
 */
@Composable
fun LongShortInsightsCard(summary: TradeSummary) {
    val insights = buildLongShortInsights(summary)
    if (insights.isEmpty()) return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryBlue.copy(alpha = 0.14f))
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Insights,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Text(
                    text = "Long / Short Insights",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

            insights.forEachIndexed { index, insight ->
                val accentColor = if (insight.isWarning) DangerRed else SuccessGreen
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 5.dp)
                            .size(6.dp)
                            .clip(RoundedCornerShape(50))
                            .background(accentColor)
                    )
                    Text(
                        text = insight.text,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 19.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (index < insights.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 16.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)
                    )
                }
            }
        }
    }
}

private data class LongShortInsight(val text: String, val isWarning: Boolean = false)

/**
 * Builds a prioritised list of insights (capped at 3) from the long/short split.
 * Returns empty when there isn't enough data to say anything meaningful.
 */
private fun buildLongShortInsights(summary: TradeSummary): List<LongShortInsight> {
    val long = summary.long
    val short = summary.short

    // Single-direction trader → one nudge, nothing to compare.
    if (long.count == 0 && short.count == 0) return emptyList()
    if (long.count == 0 || short.count == 0) {
        val onlySide = if (long.count > 0) "long" else "short"
        val other = if (onlySide == "long") "short" else "long"
        return listOf(
            LongShortInsight(
                "All of your trades are $onlySide. Log a few $other trades to reveal which direction your edge favours."
            )
        )
    }

    val result = mutableListOf<LongShortInsight>()

    // 1 ── A direction that is net negative is the most urgent thing to surface.
    val longBleeds = long.pnl < 0 && short.pnl >= 0
    val shortBleeds = short.pnl < 0 && long.pnl >= 0
    if (longBleeds || shortBleeds) {
        val (loser, winner) = if (longBleeds) "Long" to "short" else "Short" to "long"
        val loserPnl = if (longBleeds) long.pnl else short.pnl
        result += LongShortInsight(
            "$loser trades are net negative (${loserPnl.formatPnl()}) while your $winner side is green. " +
                "Tighten $loser entries or trade them smaller until they turn around.",
            isWarning = true
        )
    }

    // 2 ── Win-rate gap of 10pp+ is a clear directional edge.
    val wrGap = long.winRate - short.winRate
    if (abs(wrGap) >= 10) {
        val better = if (wrGap > 0) "long" else "short"
        val betterWr = if (wrGap > 0) long.winRate else short.winRate
        val worseWr = if (wrGap > 0) short.winRate else long.winRate
        result += LongShortInsight(
            "You win ${betterWr.formatPercent()} on ${better} trades vs ${worseWr.formatPercent()} the other way — " +
                "a ${abs(wrGap).roundToInt()}pp edge. Lean into $better setups."
        )
    }

    // 3 ── Efficiency per trade (avg P&L) — only if it adds a *new* angle.
    if (long.avgPnl != short.avgPnl) {
        val longBetter = long.avgPnl > short.avgPnl
        val better = if (longBetter) "Long" else "Short"
        val betterAvg = if (longBetter) long.avgPnl else short.avgPnl
        val worseAvg = if (longBetter) short.avgPnl else long.avgPnl
        result += LongShortInsight(
            "$better trades are more efficient at ${betterAvg.formatPnl()} per trade vs ${worseAvg.formatPnl()} the other way."
        )
    }

    // 4 ── Distribution skew: heavy on one side while the other earns more per trade.
    val majorityIsLong = long.percentage >= 65
    val majorityIsShort = short.percentage >= 65
    if (majorityIsLong && short.avgPnl > long.avgPnl) {
        result += LongShortInsight(
            "${long.percentage.roundToInt()}% of your trades are long, yet shorts earn more per trade — consider rebalancing.",
            isWarning = true
        )
    } else if (majorityIsShort && long.avgPnl > short.avgPnl) {
        result += LongShortInsight(
            "${short.percentage.roundToInt()}% of your trades are short, yet longs earn more per trade — consider rebalancing.",
            isWarning = true
        )
    }

    // Fallback when both sides look similar and nothing above fired.
    if (result.isEmpty()) {
        result += LongShortInsight(
            "Your long and short trades are performing similarly — no strong directional bias right now."
        )
    }

    return result.take(3)
}
