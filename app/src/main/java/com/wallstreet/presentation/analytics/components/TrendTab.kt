package com.wallstreet.presentation.analytics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wallstreet.core.util.formatPercent
import com.wallstreet.core.util.formatPnl
import com.wallstreet.domain.model.TrendDirection
import com.wallstreet.domain.model.TrendPerformanceData
import com.wallstreet.domain.model.TrendStat
import com.wallstreet.ui.theme.DangerRed
import com.wallstreet.ui.theme.LocalCurrencySymbol
import com.wallstreet.ui.theme.PrimaryBlue
import com.wallstreet.ui.theme.SuccessGreen
import kotlin.math.abs

// ── Meta helpers ──────────────────────────────────────────────────────────────

private data class TrendMeta(
    val label: String,
    val icon: ImageVector,
    val color: Color
)

private fun metaFor(dir: TrendDirection) = when (dir) {
    TrendDirection.UP       -> TrendMeta("Uptrend",  Icons.Default.ArrowUpward,  SuccessGreen)
    TrendDirection.DOWN     -> TrendMeta("Downtrend",Icons.Default.ArrowDownward, DangerRed)
    TrendDirection.SIDEWAYS -> TrendMeta("Sideways", Icons.Default.ArrowForward,  PrimaryBlue)
}

// ── Root composable ───────────────────────────────────────────────────────────

@Composable
fun TrendTab(data: TrendPerformanceData) {
    if (data.stats.isEmpty()) {
        EmptyTrendView()
        return
    }

    val totalTrades  = data.stats.sumOf { it.trades }
    val totalPnl     = data.stats.sumOf { it.totalPnl }
    val best         = data.stats.maxByOrNull { it.winRate }
    val worst        = data.stats.minByOrNull { it.winRate }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {


        // 2 ── Best trend banner ──────────────────────────────────────────────
        if (best != null) {
            BestTrendBanner(stat = best, totalTrades = totalTrades)
        }

        // 3 ── P&L comparison bar chart ───────────────────────────────────────
        PnlComparisonCard(stats = data.stats)

        // 4 ── Comparison matrix (all directions side-by-side) ─────────────────
        ComparisonMatrixCard(stats = data.stats, totalTrades = totalTrades)

        // 5 ── Auto-generated insights ────────────────────────────────────────
        TrendInsightsCard(
            stats        = data.stats,
            totalTrades  = totalTrades,
            best         = best,
            worst        = worst
        )

        // 6 ── Trade distribution bar ─────────────────────────────────────────
        DistributionCard(stats = data.stats, totalTrades = totalTrades)

        // 7 ── Per-direction detail cards ──────────────────────────────────────
        data.stats.forEach { stat ->
            TrendDirectionCard(stat = stat, totalTrades = totalTrades)
        }
    }
}

// ── 1. Summary strip ─────────────────────────────────────────────────────────

@Composable
private fun TrendSummaryStrip(
    totalTrades: Int,
    totalPnl: Double,
    best: TrendStat?
) {
    val pnlColor = if (totalPnl >= 0) SuccessGreen else DangerRed

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SummaryChip(
            label = "Tagged",
            value = "$totalTrades",
            valueColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        SummaryChip(
            label = "Total P&L",
            value = totalPnl.formatPnl(LocalCurrencySymbol.current),
            valueColor = pnlColor,
            modifier = Modifier.weight(1f)
        )
        if (best != null) {
            val meta = metaFor(best.direction)
            SummaryChip(
                label = "Best WR",
                value = best.winRate.formatPercent(),
                valueColor = meta.color,
                accentColor = meta.color,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SummaryChip(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
    accentColor: Color? = null
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), shape)
    ) {
        // Coloured accent bar at top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background((accentColor ?: PrimaryBlue).copy(alpha = 0.7f))
        )
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.3.sp,
                maxLines = 1
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ── 2. Best trend banner ──────────────────────────────────────────────────────

@Composable
private fun BestTrendBanner(stat: TrendStat, totalTrades: Int) {
    val meta = metaFor(stat.direction)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(meta.color.copy(alpha = 0.10f))
            .border(1.dp, meta.color.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(meta.color.copy(alpha = 0.18f))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = meta.icon,
                    contentDescription = null,
                    tint = meta.color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = meta.color,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Best condition",
                        style = MaterialTheme.typography.labelSmall,
                        color = meta.color,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = meta.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = "${stat.winRate.formatPercent()} win rate · ${stat.totalPnl.formatPnl(LocalCurrencySymbol.current)} P&L · ${stat.trades} trades",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ── 3. P&L comparison bar chart ───────────────────────────────────────────────

@Composable
private fun PnlComparisonCard(stats: List<TrendStat>) {
    val maxAbsPnl = stats.maxOf { abs(it.totalPnl) }.coerceAtLeast(0.01)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "P&L Comparison",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Profitability per market condition",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // One row per direction
            stats.forEach { stat ->
                val meta     = metaFor(stat.direction)
                val pnlColor = if (stat.totalPnl >= 0) SuccessGreen else DangerRed
                val fraction = (abs(stat.totalPnl) / maxAbsPnl).toFloat().coerceIn(0.04f, 1f)

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(meta.color.copy(alpha = 0.14f))
                                    .padding(5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = meta.icon,
                                    contentDescription = null,
                                    tint = meta.color,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Text(
                                text = meta.label,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                        Text(
                            text = stat.totalPnl.formatPnl(LocalCurrencySymbol.current),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = pnlColor,
                            maxLines = 1
                        )
                    }
                    // Bar track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(50))
                                .background(pnlColor.copy(alpha = 0.85f))
                        )
                    }
                    // Sub-label
                    Text(
                        text = "${stat.trades} trades · avg ${
                            (if (stat.trades > 0) stat.totalPnl / stat.trades else 0.0).formatPnl(LocalCurrencySymbol.current)
                        } per trade",
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ── 4. Comparison matrix ──────────────────────────────────────────────────────

@Composable
private fun ComparisonMatrixCard(stats: List<TrendStat>, totalTrades: Int) {
    val symbol = LocalCurrencySymbol.current
    // Always show all 3 directions; use a zero-stat for ones with no trades.
    val statsMap = stats.associateBy { it.direction }
    val fullStats = TrendDirection.entries.map { dir ->
        statsMap[dir] ?: TrendStat(direction = dir, trades = 0, totalPnl = 0.0, winRate = 0.0)
    }

    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Direction Comparison",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Header row: label only, neutral color, no icons
            Row(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.weight(1.2f))
                fullStats.forEach { stat ->
                    Text(
                        text = metaFor(stat.direction).label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

            // Metric rows — wrapped in their own Column so the outer spacedBy(10dp)
            // doesn't inflate the gap between every row+divider pair.
            val rowsData: List<Pair<String, (TrendStat) -> String>> = listOf(
                "Trades"     to { s -> if (s.trades > 0) "${s.trades}" else "—" },
                "Win Rate"   to { s -> if (s.trades > 0) s.winRate.formatPercent() else "—" },
                "Total P&L"  to { s -> if (s.trades > 0) s.totalPnl.formatPnl(symbol) else "—" },
                "Avg P&L"    to { s -> if (s.trades > 0) (s.totalPnl / s.trades).formatPnl(symbol) else "—" },
                "% of Total" to { s ->
                    if (s.trades > 0 && totalTrades > 0)
                        "${(s.trades.toFloat() / totalTrades * 100).toInt()}%"
                    else "—"
                }
            )

            Column {
                rowsData.forEachIndexed { rowIndex, (rowLabel, valueFor) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = rowLabel,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1.2f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        fullStats.forEach { stat ->
                            val value = valueFor(stat)
                            val valueColor = when {
                                value == "—" -> MaterialTheme.colorScheme.onSurfaceVariant
                                rowLabel == "Win Rate" -> when {
                                    stat.winRate >= 60 -> SuccessGreen
                                    stat.winRate < 40  -> DangerRed
                                    else               -> PrimaryBlue
                                }
                                rowLabel == "Total P&L" ->
                                    if (stat.totalPnl >= 0) SuccessGreen else DangerRed
                                rowLabel == "Avg P&L" ->
                                    if (stat.trades > 0 && stat.totalPnl / stat.trades >= 0) SuccessGreen else DangerRed
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                            Text(
                                text = value,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = valueColor,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    if (rowIndex < rowsData.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 5.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)
                        )
                    }
                }
            }
        }
    }
}

// ── 5. Auto-generated insights ────────────────────────────────────────────────

private data class Insight(val text: String, val isWarning: Boolean = false)

@Composable
private fun TrendInsightsCard(
    stats: List<TrendStat>,
    totalTrades: Int,
    best: TrendStat?,
    worst: TrendStat?
) {
    val insights = buildInsights(stats, totalTrades, best, worst, LocalCurrencySymbol.current)
    if (insights.isEmpty()) return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Card header
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
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Text(
                    text = "Trading Insights",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

            // Insight rows
            insights.forEachIndexed { index, insight ->
                val accentColor = if (insight.isWarning) DangerRed else SuccessGreen

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 3.dp)
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

private fun buildInsights(
    stats: List<TrendStat>,
    totalTrades: Int,
    best: TrendStat?,
    worst: TrendStat?,
    symbol: String = "$"
): List<Insight> {
    val result = mutableListOf<Insight>()

    // 1. Win rate gap between best and worst
    if (best != null && worst != null && best.direction != worst.direction) {
        val diff = best.winRate - worst.winRate
        if (diff >= 10) {
            val bestMeta  = metaFor(best.direction)
            val worstMeta = metaFor(worst.direction)
            result += Insight(
                "Your win rate in ${bestMeta.label} (${best.winRate.formatPercent()}) " +
                "is ${diff.toInt()}pp higher than ${worstMeta.label} " +
                "(${worst.winRate.formatPercent()}) — lean into ${bestMeta.label} conditions.",
                isWarning = false
            )
        }
    }

    // 2. Best avg P&L direction
    val bestAvg = stats.maxByOrNull {
        if (it.trades > 0) it.totalPnl / it.trades else Double.NEGATIVE_INFINITY
    }
    if (bestAvg != null && bestAvg.trades > 0) {
        val avg = bestAvg.totalPnl / bestAvg.trades
        if (avg > 0) {
            result += Insight(
                "Your highest avg P&L per trade (${avg.formatPnl(symbol)}) comes from " +
                "${metaFor(bestAvg.direction).label} conditions — ${bestAvg.trades} trades logged."
            )
        }
    }

    // 3. Poor-performing direction warning
    val poorStat = stats
        .filter { it.trades >= 3 }
        .minByOrNull { it.winRate }
    if (poorStat != null && poorStat.winRate < 45) {
        result += Insight(
            "${metaFor(poorStat.direction).label} markets show only " +
            "${poorStat.winRate.formatPercent()} win rate over ${poorStat.trades} trades. " +
            "Consider tightening risk management in these conditions.",
            isWarning = true
        )
    }

    // 4. Dominant condition (if one direction has ≥ 50% of tagged trades)
    if (totalTrades > 0) {
        val dominant = stats.maxByOrNull { it.trades }
        if (dominant != null) {
            val pct = (dominant.trades.toFloat() / totalTrades * 100).toInt()
            if (pct >= 55) {
                result += Insight(
                    "$pct% of your tagged trades are taken in ${metaFor(dominant.direction).label} " +
                    "conditions — make sure you're also tracking other market environments."
                )
            }
        }
    }

    // 5. Negative total P&L direction
    val loser = stats.filter { it.trades >= 2 }.minByOrNull { it.totalPnl }
    if (loser != null && loser.totalPnl < 0) {
        result += Insight(
            "${metaFor(loser.direction).label} trades have produced ${loser.totalPnl.formatPnl(symbol)} " +
            "total loss. Review your entries and exits in these conditions.",
            isWarning = true
        )
    }

    return result.take(4) // cap at 4 insights to keep card compact
}

// ── 6. Trade distribution bar ─────────────────────────────────────────────────

@Composable
private fun DistributionCard(stats: List<TrendStat>, totalTrades: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Trade Distribution",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "How your $totalTrades trades spread across market conditions",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Stacked bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(50))
            ) {
                stats.forEach { stat ->
                    val fraction = (stat.trades.toFloat() / totalTrades).coerceAtLeast(0.01f)
                    Box(
                        modifier = Modifier
                            .weight(fraction)
                            .fillMaxHeight()
                            .background(metaFor(stat.direction).color)
                    )
                }
            }

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                stats.forEach { stat ->
                    val meta = metaFor(stat.direction)
                    val pct  = (stat.trades.toFloat() / totalTrades * 100).toInt()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(meta.color)
                        )
                        Text(
                            text = "${meta.label} $pct%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = meta.color,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

// ── 7. Per-direction detail card ──────────────────────────────────────────────

@Composable
private fun TrendDirectionCard(stat: TrendStat, totalTrades: Int) {
    val meta       = metaFor(stat.direction)
    val pnlColor   = if (stat.totalPnl >= 0) SuccessGreen else DangerRed
    val pct        = (stat.trades.toFloat() / totalTrades * 100).toInt()
    val avgPnl     = if (stat.trades > 0) stat.totalPnl / stat.trades else 0.0
    val winFraction = (stat.winRate / 100f).coerceIn(0.0, 1.0)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(meta.color.copy(alpha = 0.14f))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = meta.icon,
                            contentDescription = null,
                            tint = meta.color,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Column {
                        Text(
                            text = meta.label,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Text(
                            text = "${stat.trades} trades · $pct% of total",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
                Text(
                    text = stat.totalPnl.formatPnl(LocalCurrencySymbol.current),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = pnlColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatColumn(label = "WIN RATE", value = stat.winRate.formatPercent(), color = meta.color)
                StatColumn(label = "TRADES",   value = "${stat.trades}",             color = MaterialTheme.colorScheme.onSurface)
                StatColumn(label = "AVG P&L",  value = avgPnl.formatPnl(LocalCurrencySymbol.current),           color = if (avgPnl >= 0) SuccessGreen else DangerRed)
            }

            // Win rate bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Win rate",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.4.sp
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                ) {
                    Box(
                        modifier = Modifier
                            .weight(winFraction.coerceAtLeast(0.01).toFloat())
                            .fillMaxHeight()
                            .background(meta.color)
                    )
                    Box(
                        modifier = Modifier
                            .weight((1f - winFraction).coerceAtLeast(0.01).toFloat())
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.background)
                    )
                }
            }
        }
    }
}

// ── Shared stat column ────────────────────────────────────────────────────────

@Composable
private fun StatColumn(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 9.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.4.sp,
            maxLines = 1
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyTrendView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(PrimaryBlue.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = "No trend data yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "When logging a trade, select the market trend\n(Up, Down, or Sideways) to unlock this analysis.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}
