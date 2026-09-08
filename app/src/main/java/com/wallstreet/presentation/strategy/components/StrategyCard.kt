package com.wallstreet.presentation.strategy.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.wallstreet.core.util.HapticStyle
import com.wallstreet.core.util.hapticClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wallstreet.core.util.format
import com.wallstreet.core.util.formatPercent
import com.wallstreet.core.util.formatPnl
import com.wallstreet.domain.model.insights.StrategyVerdict
import com.wallstreet.domain.model.strategy.StrategyStats
import com.wallstreet.ui.theme.DangerRed
import com.wallstreet.ui.theme.LocalCurrencySymbol
import com.wallstreet.ui.theme.PrimaryBlue
import com.wallstreet.ui.theme.SuccessGreen
import com.wallstreet.ui.theme.White

@Composable
fun StrategyCard(
    stats: StrategyStats,
    rank: Int,
    onStrategyClick: () -> Unit,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit,
    verdict: StrategyVerdict? = null,
) {
    val pnlColor = if (stats.totalPnl >= 0) SuccessGreen else DangerRed

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(0.10f),
                spotColor = Color.Black.copy(0.12f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f), RoundedCornerShape(16.dp))
            .hapticClickable(HapticStyle.Light) { if (isSelectionMode) onSelectionChanged(!isSelected) else onStrategyClick() }
    ) {
        // ── Name row: checkbox? + name (left) + rank badge (right) ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isSelectionMode) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .border(
                                1.5.dp,
                                if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                RoundedCornerShape(4.dp)
                            )
                            .background(if (isSelected) PrimaryBlue else Color.Transparent)
                            .hapticClickable(HapticStyle.Light) { onSelectionChanged(!isSelected) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) Icon(Icons.Default.Check, null, tint = White, modifier = Modifier.size(13.dp))
                    }
                }
                Text(
                    text = stats.strategy.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    letterSpacing = (-0.3).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                VerdictPill(verdict)
            }
            RankBadge(rank = rank)
        }

        // ── Hero: P&L (left) + sparkline (right) ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 0.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = stats.totalPnl.formatPnl(LocalCurrencySymbol.current),
                fontWeight = FontWeight.ExtraBold,
                color = pnlColor,
                fontSize = 26.sp,
                letterSpacing = (-0.6).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (stats.sparkline.size >= 2) {
                Sparkline(
                    points = stats.sparkline,
                    color = pnlColor,
                    modifier = Modifier
                        .width(120.dp)
                        .height(52.dp)
                )
            }
        }

        // ── Stats footer (inset) ──
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f))
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.Top
        ) {
            val winRateColor = when {
                stats.winRate >= 60 -> SuccessGreen
                stats.winRate < 40  -> DangerRed
                else                -> PrimaryBlue
            }
            val rrColor = when {
                stats.rrRatio >= 2.0 -> SuccessGreen
                stats.rrRatio >= 1.0 -> PrimaryBlue
                else                 -> DangerRed
            }
            val rrDisplay = if (stats.rrRatio == 0.0) "N/A" else "1:${stats.rrRatio.format(1)}"

            StatCell(
                label = "WIN RATE",
                value = stats.winRate.formatPercent(),
                valueColor = MaterialTheme.colorScheme.onSurface,
                bar = (stats.winRate / 100.0).toFloat(),
                barColor = winRateColor,
                modifier = Modifier.weight(1f)
            )
            VerticalDivider(
                modifier = Modifier.fillMaxHeight().padding(horizontal = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
            StatCell(
                label = "TRADES",
                value = "${stats.totalTrades}",
                valueColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            VerticalDivider(
                modifier = Modifier.fillMaxHeight().padding(horizontal = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
            StatCell(
                label = "R:R",
                value = rrDisplay,
                valueColor = if (stats.rrRatio == 0.0) MaterialTheme.colorScheme.onSurfaceVariant else rrColor,
                modifier = Modifier.weight(1f)
            )
            VerticalDivider(
                modifier = Modifier.fillMaxHeight().padding(horizontal = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
            StatCell(
                label = "AVG P&L",
                value = stats.avgProfitPerTrade.formatPnl(LocalCurrencySymbol.current),
                valueColor = if (stats.avgProfitPerTrade >= 0) SuccessGreen else DangerRed,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun VerdictPill(verdict: StrategyVerdict?) {
    if (verdict == null) return
    val (label, color) = when (verdict) {
        StrategyVerdict.SCALE_UP -> "Scale up" to SuccessGreen
        StrategyVerdict.KEEP -> "Keep" to PrimaryBlue
        StrategyVerdict.REVIEW -> "Review" to Color(0xFFEA580C)
        StrategyVerdict.DROP -> "Drop" to DangerRed
        StrategyVerdict.NEEDS_MORE_DATA -> "More data" to Color(0xFF64748B)
    }
    Text(
        text = label,
        color = color,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        letterSpacing = 0.2.sp,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

@Composable
private fun RankBadge(rank: Int) {
    when (rank) {
        1 -> RankPill("🏆", "Rank #1", Color(0xFFD97706).copy(alpha = 0.18f), Color(0xFFD97706))
        2 -> RankPill("🥈", "Rank #2", Color(0xFF64748B).copy(alpha = 0.22f), Color(0xFF475569))
        3 -> RankPill("🥉", "Rank #3", Color(0xFFEA580C).copy(alpha = 0.18f), Color(0xFFEA580C))
        else -> Text(
            text = "#$rank",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 0.3.sp
        )
    }
}

@Composable
private fun RankPill(emoji: String, label: String, containerColor: Color, contentColor: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(containerColor)
            .padding(horizontal = 9.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = emoji, fontSize = 11.sp)
        Text(
            text = label,
            color = contentColor,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 0.2.sp
        )
    }
}

@Composable
private fun Sparkline(
    points: List<Double>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Always anchor the visible range to zero and add 20% breathing room on
        // whichever side has data. This prevents a monotonically winning/losing
        // series from filling corner-to-corner as a straight diagonal.
        val dataMax = points.max()
        val dataMin = points.min()
        val visMax = maxOf(dataMax, 0.0).let { if (it == 0.0) 1.0 else it * 1.20 }
        val visMin = minOf(dataMin, 0.0).let { if (it == 0.0) -1.0 else it * 1.20 }
        val range = visMax - visMin

        val vPad = h * 0.08f  // 8% top/bottom so the line never hugs the edge
        val drawH = h - vPad * 2

        fun xOf(i: Int) = i.toFloat() / (points.size - 1) * w
        fun yOf(v: Double) = vPad + ((visMax - v) / range * drawH).toFloat()

        // Subtle dashed zero baseline — only draw when zero is meaningfully inside
        // the chart (i.e. there are both positive and negative values)
        if (dataMin < 0.0 && dataMax > 0.0) {
            drawLine(
                color = color.copy(alpha = 0.25f),
                start = Offset(0f, yOf(0.0)),
                end = Offset(w, yOf(0.0)),
                strokeWidth = 0.8.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(3.dp.toPx(), 3.dp.toPx())
                )
            )
        }

        // Build smooth bezier path
        val linePath = Path().apply {
            moveTo(xOf(0), yOf(points[0]))
            for (i in 1 until points.size) {
                val x1 = xOf(i - 1); val y1 = yOf(points[i - 1])
                val x2 = xOf(i);     val y2 = yOf(points[i])
                val mx = (x1 + x2) / 2f
                cubicTo(mx, y1, mx, y2, x2, y2)
            }
        }

        // Gradient fill anchored to the zero line, not the canvas bottom
        val zeroY = yOf(0.0).coerceIn(0f, h)
        val fillPath = Path().apply {
            addPath(linePath)
            lineTo(w, zeroY)
            lineTo(0f, zeroY)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(color.copy(alpha = 0.25f), color.copy(alpha = 0f)),
                startY = minOf(yOf(dataMax.toFloat().toDouble()), zeroY),
                endY = maxOf(yOf(dataMin.toFloat().toDouble()), zeroY)
            )
        )

        // Line
        drawPath(
            path = linePath,
            color = color,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // End dot
        drawCircle(
            color = color,
            radius = 2.8.dp.toPx(),
            center = Offset(xOf(points.size - 1), yOf(points.last()))
        )
    }
}

@Composable
private fun StatCell(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
    bar: Float? = null,
    barColor: Color? = null
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.4.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            color = valueColor,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (bar != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(bar.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .background(barColor ?: valueColor)
                )
            }
        }
    }
}
