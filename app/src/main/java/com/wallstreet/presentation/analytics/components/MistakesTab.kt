package com.wallstreet.presentation.analytics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wallstreet.core.util.formatPercent
import com.wallstreet.core.util.formatPnl
import com.wallstreet.domain.model.MistakeStat
import com.wallstreet.domain.model.MistakesAnalysisData
import com.wallstreet.domain.model.insights.Insight
import com.wallstreet.domain.model.insights.InsightSeverity
import com.wallstreet.domain.model.insights.MistakeComparison
import com.wallstreet.presentation.components.InsightsCard
import com.wallstreet.ui.theme.DangerRed
import com.wallstreet.ui.theme.LocalCurrencySymbol
import com.wallstreet.ui.theme.SuccessGreen
import com.wallstreet.ui.theme.WarningOrange
import kotlin.math.abs

@Composable
fun MistakesTab(
    data: MistakesAnalysisData,
    insights: List<Insight> = emptyList(),
    comparisons: List<MistakeComparison> = emptyList(),
    currencySymbol: String = "$",
) {
    val ranked = data.topMistakes.filter { it.count > 0 }

    if (ranked.isEmpty() && insights.isEmpty()) {
        EmptyMistakesView()
        return
    }

    val worsening = insights.filter {
        (it.id.startsWith("mistake.repeating") || it.id.startsWith("mistake.new") ||
            it.id == "mistake.overallRate") && it.severity != InsightSeverity.POSITIVE
    }
    val improving = insights.filter {
        it.id.startsWith("mistake.reducing") || it.id.startsWith("mistake.cleared") ||
            (it.id == "mistake.overallRate" && it.severity == InsightSeverity.POSITIVE)
    }
    val coaching = insights.filter {
        it.id == "mistake.costliest" || it.id.startsWith("mistake.doNotEnter") ||
            it.id == "mistake.concentration"
    }
    val cleanEdge = insights.firstOrNull { it.id == "mistake.cleanEdge" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MistakeSummaryStrip(data = data)

        when {
            cleanEdge != null -> DisciplineBanner(text = cleanEdge.body)
            data.cleanTradeWinRate > 0.0 -> DisciplineBanner(
                text = "Your trades with no mistake tagged win " +
                    "${data.cleanTradeWinRate.formatPercent()}. That discipline is the edge."
            )
        }

        if (ranked.isNotEmpty()) {
            CostRankedMistakesCard(
                ranked = ranked,
                comparisons = comparisons.associateBy { it.name },
                symbol = currencySymbol,
            )
        }

        MovementSection(title = "Still costing you", icon = Icons.Default.TrendingUp, insights = worsening)
        MovementSection(title = "Improving", icon = Icons.Default.TrendingDown, insights = improving)

        InsightsCard(title = "Coaching", insights = coaching)
    }
}

// ── Summary strip ─────────────────────────────────────────────────────────────

@Composable
private fun MistakeSummaryStrip(data: MistakesAnalysisData) {
    val costly = data.mostCostlyMistakes
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MistakeChip(
            label = "CLEAN WR",
            value = data.cleanTradeWinRate.formatPercent(),
            valueColor = SuccessGreen,
            accentColor = SuccessGreen,
            modifier = Modifier.weight(1f)
        )
        MistakeChip(
            label = "MISTAKE TRADES",
            value = "${data.totalMistakeTrades}",
            valueColor = MaterialTheme.colorScheme.onSurface,
            accentColor = WarningOrange,
            modifier = Modifier.weight(1f)
        )
        MistakeChip(
            label = "COSTLIEST",
            value = if (costly.count > 0) costly.totalPnlImpact.formatPnl(LocalCurrencySymbol.current) else "—",
            valueColor = if (costly.count > 0 && costly.totalPnlImpact < 0) DangerRed
            else MaterialTheme.colorScheme.onSurface,
            accentColor = DangerRed,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MistakeChip(
    label: String,
    value: String,
    valueColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), shape)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(accentColor.copy(alpha = 0.7f))
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
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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

// ── Discipline banner ─────────────────────────────────────────────────────────

@Composable
private fun DisciplineBanner(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SuccessGreen.copy(alpha = 0.10f))
            .border(1.dp, SuccessGreen.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(SuccessGreen.copy(alpha = 0.18f))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                text = text,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp,
            )
        }
    }
}

// ── Cost-ranked mistakes ──────────────────────────────────────────────────────

@Composable
private fun CostRankedMistakesCard(
    ranked: List<MistakeStat>,
    comparisons: Map<String, MistakeComparison>,
    symbol: String,
) {
    val byImpact = ranked.sortedBy { it.totalPnlImpact }
    val maxAbsImpact = byImpact.maxOf { abs(it.totalPnlImpact) }.coerceAtLeast(1.0)
    val midpoint = (byImpact.size + 1) / 2

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DangerRed.copy(alpha = 0.14f))
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = DangerRed,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = "Mistakes by impact",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Total P&L cost · change vs last period",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

            byImpact.forEachIndexed { index, stat ->
                MistakeRow(
                    rank = index + 1,
                    stat = stat,
                    maxAbsImpact = maxAbsImpact,
                    accentColor = if (index < midpoint) DangerRed else WarningOrange,
                    comparison = comparisons[stat.name],
                    symbol = symbol,
                )
            }
        }
    }
}

@Composable
private fun MistakeRow(
    rank: Int,
    stat: MistakeStat,
    maxAbsImpact: Double,
    accentColor: Color,
    comparison: MistakeComparison?,
    symbol: String,
) {
    val barFraction = (abs(stat.totalPnlImpact) / maxAbsImpact).toFloat().coerceIn(0f, 1f)
    val impactColor = if (stat.totalPnlImpact < 0) DangerRed else SuccessGreen

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$rank",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.widthIn(min = 16.dp).padding(top = 1.dp),
            fontSize = 12.sp,
            maxLines = 1
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = stat.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                DeltaPill(comparison)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(barFraction)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(accentColor)
                )
            }
            Text(
                text = "avg ${stat.avgPnlImpact.formatPnl(symbol)} · ${stat.winRate.formatPercent()} WR",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = stat.totalPnlImpact.formatPnl(symbol),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = impactColor,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${stat.count}×",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun DeltaPill(c: MistakeComparison?) {
    if (c == null) return
    val (label, color) = when {
        c.isNew -> "NEW" to WarningOrange
        c.delta > 0 -> "▲${c.delta}" to DangerRed
        c.delta < 0 -> "▼${-c.delta}" to SuccessGreen
        else -> return
    }
    Text(
        text = label,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 5.dp, vertical = 1.dp),
        maxLines = 1,
    )
}

// ── Movement section (repeating / reducing) ───────────────────────────────────

@Composable
private fun MovementSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    insights: List<Insight>,
) {
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
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
            insights.forEachIndexed { index, item ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 5.dp)
                            .size(6.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (item.severity == InsightSeverity.POSITIVE) SuccessGreen else DangerRed
                            )
                    )
                    Text(
                        text = item.body,
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

// ── Empty state ──────────────────────────────────────────────────────────────

@Composable
private fun EmptyMistakesView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(SuccessGreen.copy(alpha = 0.12f))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = SuccessGreen,
                modifier = Modifier.size(36.dp)
            )
        }
        Text(
            text = "No mistakes logged yet",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = "Tag mistakes when logging trades to unlock a breakdown of what's costing you and how to fix it.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 19.sp,
            modifier = Modifier.padding(top = 6.dp),
            overflow = TextOverflow.Ellipsis
        )
    }
}
