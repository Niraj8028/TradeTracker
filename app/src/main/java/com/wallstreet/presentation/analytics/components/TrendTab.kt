package com.wallstreet.presentation.analytics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wallstreet.core.util.formatPercent
import com.wallstreet.core.util.formatPnl
import com.wallstreet.domain.model.TrendDirection
import com.wallstreet.domain.model.TrendPerformanceData
import com.wallstreet.domain.model.TrendStat
import com.wallstreet.ui.theme.DangerRed
import com.wallstreet.ui.theme.PrimaryBlue
import com.wallstreet.ui.theme.SuccessGreen

private data class TrendMeta(
    val label: String,
    val icon: ImageVector,
    val color: androidx.compose.ui.graphics.Color
)

private fun metaFor(dir: TrendDirection) = when (dir) {
    TrendDirection.UP -> TrendMeta("Uptrend", Icons.Default.ArrowUpward, SuccessGreen)
    TrendDirection.DOWN -> TrendMeta("Downtrend", Icons.Default.ArrowDownward, DangerRed)
    TrendDirection.SIDEWAYS -> TrendMeta("Sideways", Icons.Default.ArrowForward, PrimaryBlue)
}

@Composable
fun TrendTab(data: TrendPerformanceData) {
    if (data.stats.isEmpty()) {
        EmptyTrendView()
        return
    }

    val totalTrades = data.stats.sumOf { it.trades }
    val best = data.stats.maxByOrNull { it.winRate }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Best trend highlight ─────────────────────────────────────────────
        if (best != null) {
            BestTrendBanner(stat = best, totalTrades = totalTrades)
        }

        // ── Distribution bar ────────────────────────────────────────────────
        DistributionCard(stats = data.stats, totalTrades = totalTrades)

        // ── Per-direction detail cards ───────────────────────────────────────
        data.stats.forEach { stat ->
            TrendDirectionCard(stat = stat, totalTrades = totalTrades)
        }
    }
}

@Composable
private fun BestTrendBanner(stat: TrendStat, totalTrades: Int) {
    val meta = metaFor(stat.direction)
    val pnlColor = if (stat.totalPnl >= 0) SuccessGreen else DangerRed

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(meta.color.copy(alpha = 0.10f))
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
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
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
                        text = "Best trend",
                        style = MaterialTheme.typography.labelSmall,
                        color = meta.color,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = meta.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${stat.winRate.formatPercent()} win rate · ${stat.totalPnl.formatPnl()} P&L · ${stat.trades} trades",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

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
                    val pct = (stat.trades.toFloat() / totalTrades * 100).toInt()
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
                            color = meta.color
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendDirectionCard(stat: TrendStat, totalTrades: Int) {
    val meta = metaFor(stat.direction)
    val pnlColor = if (stat.totalPnl >= 0) SuccessGreen else DangerRed
    val pct = (stat.trades.toFloat() / totalTrades * 100).toInt()
    val avgPnl = if (stat.trades > 0) stat.totalPnl / stat.trades else 0.0
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
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${stat.trades} trades · $pct% of total",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = stat.totalPnl.formatPnl(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = pnlColor
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatColumn(label = "WIN RATE", value = stat.winRate.formatPercent(), color = meta.color)
                StatColumn(label = "TRADES", value = "${stat.trades}", color = MaterialTheme.colorScheme.onSurface)
                StatColumn(label = "AVG P&L", value = avgPnl.formatPnl(), color = if (avgPnl >= 0) SuccessGreen else DangerRed)
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

@Composable
private fun StatColumn(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 9.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.4.sp
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
    }
}

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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "No trend data yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "When logging a trade, select the market trend (Up, Down, or Sideways) to unlock this analysis.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
