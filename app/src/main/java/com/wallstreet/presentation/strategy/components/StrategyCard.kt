package com.wallstreet.presentation.strategy.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wallstreet.core.util.format
import com.wallstreet.core.util.formatPercent
import com.wallstreet.core.util.formatPnl
import com.wallstreet.domain.model.strategy.StrategyStats
import com.wallstreet.ui.theme.DangerRed
import com.wallstreet.ui.theme.PrimaryBlue
import com.wallstreet.ui.theme.SuccessGreen

@Composable
fun StrategyCard(
    stats: StrategyStats,
    onStrategyClick: () -> Unit,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(alpha = 0.3f),
                spotColor = Color.Black.copy(alpha = 0.3f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        StrategyCardHeader(stats = stats)
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
            thickness = 0.5.dp
        )
        MetricsRow(stats = stats)
        // Detail button
        DetailButton(onClick = onStrategyClick)
    }
}

@Composable
fun StrategyCardHeader(stats: StrategyStats) {
    val isPnlPositive = stats.totalPnl > 0
    val pnlColor = if (isPnlPositive) SuccessGreen else DangerRed

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stats.strategy.name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stats.totalPnl.formatPnl(),
            style = MaterialTheme.typography.titleMedium,
            color = pnlColor
        )
    }
}

@Composable
private fun MetricsRow(stats: StrategyStats) {
    val rrDisplay = if (stats.rrRatio == 0.0) "∞" else stats.rrRatio.format(1)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MetricItem(
            label = "Win Rate",
            value = stats.winRate.formatPercent()
        )
        MetricItem(
            label = "Total Trades",
            value = "${stats.totalTrades}"
        )
        MetricItem(
            label = "R/R Ratio",
            value = rrDisplay
        )
        MetricItem(
            label = "Avg Profit",
            value = stats.avgProfitPerTrade.formatPnl()
        )
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DetailButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryBlue.copy(alpha = 0.15f),
            contentColor = PrimaryBlue
        ),
        shape = RoundedCornerShape(10.dp),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Text(
            text = "Detailed Analysis",
            style = MaterialTheme.typography.labelLarge,
            color = PrimaryBlue
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "→",
            style = MaterialTheme.typography.labelLarge,
            color = PrimaryBlue
        )
    }
}