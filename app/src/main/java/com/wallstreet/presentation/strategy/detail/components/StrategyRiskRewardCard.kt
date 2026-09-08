package com.wallstreet.presentation.strategy.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wallstreet.domain.model.HomeStats
import com.wallstreet.ui.theme.DangerRed
import com.wallstreet.ui.theme.PrimaryBlue
import com.wallstreet.ui.theme.SuccessGreen
import com.wallstreet.ui.theme.WarningOrange

@Composable
fun StrategyRiskRewardCard(
    stats: HomeStats,
    profitFactor: Double,
    maxDrawdown: Double,
    winStreak: Int,
    modifier: Modifier = Modifier
) {
    val isPnlPositive = stats.totalPnl >= 0
    val pnlColor = if (isPnlPositive) SuccessGreen else DangerRed
    val pnlText = if (isPnlPositive)
        "+$${"%.2f".format(stats.totalPnl)}"
    else
        "-$${"%.2f".format(-stats.totalPnl)}"

    val shape = RoundedCornerShape(16.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 3.dp,
                shape = shape,
                ambientColor = Color.Black.copy(0.25f),
                spotColor = Color.Black.copy(0.25f)
            )
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), shape)
    ) {
        // P&L summary header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "TOTAL P&L",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    letterSpacing = 0.8.sp,
                    maxLines = 1
                )
                Text(
                    text = pnlText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = pnlColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${stats.totalTrades} trades · ${"%.1f".format(stats.winRate)}% win",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(pnlColor.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "${stats.totalWinningTrades}W · ${stats.totalLosingTrades}L",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = pnlColor
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

        // Metrics grid
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                val pfColor = when {
                    profitFactor >= 2.0 -> SuccessGreen
                    profitFactor >= 1.0 -> PrimaryBlue
                    else -> DangerRed
                }
                MetricTile(
                    label = "Profit Factor",
                    value = if (profitFactor >= 999.0) "∞" else "${"%.2f".format(profitFactor)}x",
                    valueColor = pfColor,
                    accentColor = pfColor,
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = "Avg Win",
                    value = "+$${"%.0f".format(stats.avgProfit)}",
                    valueColor = SuccessGreen,
                    accentColor = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricTile(
                    label = "Avg Loss",
                    value = "-$${"%.0f".format(stats.avgLoss)}",
                    valueColor = DangerRed,
                    accentColor = DangerRed,
                    modifier = Modifier.weight(1f)
                )
                val ddColor = when {
                    maxDrawdown == 0.0 -> SuccessGreen
                    maxDrawdown < 500.0 -> WarningOrange
                    else -> DangerRed
                }
                MetricTile(
                    label = "Max Drawdown",
                    value = "-$${"%.0f".format(maxDrawdown)}",
                    valueColor = ddColor,
                    accentColor = ddColor,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricTile(
                    label = "Best Win Streak",
                    value = "$winStreak ${if (winStreak == 1) "trade" else "trades"}",
                    valueColor = PrimaryBlue,
                    accentColor = PrimaryBlue,
                    modifier = Modifier.weight(1f)
                )
                val rrColor = when {
                    stats.riskRewardRatio >= 2.0 -> SuccessGreen
                    stats.riskRewardRatio >= 1.0 -> PrimaryBlue
                    else -> DangerRed
                }
                MetricTile(
                    label = "Risk / Reward",
                    value = "${"%.1f".format(stats.riskRewardRatio)}x",
                    valueColor = rrColor,
                    accentColor = rrColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetricTile(
    label: String,
    value: String,
    valueColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(10.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), shape)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(accentColor.copy(alpha = 0.6f))
        )
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
