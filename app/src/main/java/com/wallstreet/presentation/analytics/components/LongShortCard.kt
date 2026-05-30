package com.wallstreet.presentation.analytics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wallstreet.ui.theme.DangerRed
import com.wallstreet.ui.theme.BorderColors
import com.wallstreet.ui.theme.SuccessGreen
import com.wallstreet.domain.model.TradeSummary
import com.wallstreet.core.util.formatPnl

@Composable
fun LongShortCard(summary: TradeSummary) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

            // ── Header ──────────────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Long vs Short",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Direction distribution · ${summary.totalTrades} trades",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ── Stacked progress bar ─────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
            ) {
                Box(
                    modifier = Modifier
                        .weight(summary.long.percentage.toFloat().coerceAtLeast(0.01f))
                        .fillMaxHeight()
                        .background(SuccessGreen)      // 0xFF10B981
                )
                Box(
                    modifier = Modifier
                        .weight(summary.short.percentage.toFloat().coerceAtLeast(0.01f))
                        .fillMaxHeight()
                        .background(DangerRed)         // 0xFFEF4444
                )
            }

            // ── Legend labels ────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LegendDot(
                    color = SuccessGreen,
                    label = "Long ${summary.long.percentage.toInt()}%"
                )
                LegendDot(
                    color = DangerRed,
                    label = "Short ${summary.short.percentage.toInt()}%",
                    dotOnRight = true
                )
            }

            // ── Divider ───────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(BorderColors.current.secondary)
            )

            // ── Long | Short detail columns ───────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                DirectionDetail(
                    modifier = Modifier.weight(1f),
                    label = "Long",
                    percentage = summary.long.percentage.toInt(),
                    pnl = summary.long.pnl,
                    tradeCount = summary.long.count,
                    winRate = summary.long.winRate.toInt(),
                    accentColor = SuccessGreen,
                    paddingEnd = true
                )

                // Vertical divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(100.dp)
                        .align(Alignment.CenterVertically)
                        .background(BorderColors.current.secondary)
                )

                DirectionDetail(
                    modifier = Modifier.weight(1f),
                    label = "Short",
                    percentage = summary.short.percentage.toInt(),
                    pnl = summary.short.pnl,
                    tradeCount = summary.short.count,
                    winRate = summary.short.winRate.toInt(),
                    accentColor = DangerRed,
                    paddingEnd = false
                )
            }
        }
    }
}

@Composable
private fun LegendDot(
    color: Color,
    label: String,
    dotOnRight: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (!dotOnRight) {
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(color)
            )
        }
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
        if (dotOnRight) {
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(color)
            )
        }
    }
}

@Composable
private fun DirectionDetail(
    modifier: Modifier = Modifier,
    label: String,
    percentage: Int,
    pnl: Double,
    tradeCount: Int,
    winRate: Int,
    accentColor: Color,
    paddingEnd: Boolean
) {
    val pnlFormatted = pnl.formatPnl()
    val pnlColor = if (pnl >= 0) SuccessGreen else DangerRed

    Column(
        modifier = modifier.padding(
            start = if (!paddingEnd) 16.dp else 0.dp,
            end = if (paddingEnd) 16.dp else 0.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Label + percentage
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$percentage%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // PnL
        Text(
            text = pnlFormatted,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = pnlColor,
            lineHeight = 22.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Trade count + win rate row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$tradeCount trades",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "WR $winRate%",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Mini win-rate progress bar
        val winFraction = (winRate / 100f).coerceIn(0f, 1f)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(50))
        ) {
            Box(
                modifier = Modifier
                    .weight(winFraction.coerceAtLeast(0.01f))
                    .fillMaxHeight()
                    .background(accentColor)
            )
            Box(
                modifier = Modifier
                    .weight((1f - winFraction).coerceAtLeast(0.01f))
                    .fillMaxHeight()
                    // DarkSurfaceVariant = 0xFF1F2630 — the subtle empty-track color
                    .background(MaterialTheme.colorScheme.background)
            )
        }
    }
}
