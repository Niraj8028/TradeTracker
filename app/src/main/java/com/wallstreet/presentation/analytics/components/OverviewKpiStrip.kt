package com.wallstreet.presentation.analytics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wallstreet.core.util.formatPercent
import com.wallstreet.core.util.formatPnl
import com.wallstreet.domain.model.OverviewStats
import com.wallstreet.ui.theme.DangerRed
import com.wallstreet.ui.theme.LocalCurrencySymbol
import com.wallstreet.ui.theme.PrimaryBlue
import com.wallstreet.ui.theme.SuccessGreen
import java.util.Locale

/**
 * "At a glance" KPI hero strip for the Overview tab — Net P&L, Win Rate,
 * Trades and Profit Factor in a 2×2 grid of accent-bar chips.
 */
@Composable
fun OverviewKpiStrip(stats: OverviewStats) {
    val pnlColor = if (stats.netPnl >= 0) SuccessGreen else DangerRed
    val pfColor = when {
        stats.profitFactor == null -> PrimaryBlue
        stats.profitFactor >= 1.0 -> SuccessGreen
        else -> DangerRed
    }
    val pfValue = stats.profitFactor
        ?.let { String.format(Locale.US, "%.2f", it) }
        ?: "—"

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiChip(
                label = "NET P&L",
                value = stats.netPnl.formatPnl(LocalCurrencySymbol.current),
                valueColor = pnlColor,
                accentColor = pnlColor,
                modifier = Modifier.weight(1f)
            )
            KpiChip(
                label = "WIN RATE",
                value = stats.winRate.formatPercent(),
                valueColor = MaterialTheme.colorScheme.onSurface,
                accentColor = PrimaryBlue,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiChip(
                label = "TRADES",
                value = "${stats.totalTrades}",
                valueColor = MaterialTheme.colorScheme.onSurface,
                accentColor = PrimaryBlue,
                modifier = Modifier.weight(1f)
            )
            KpiChip(
                label = "PROFIT FACTOR",
                value = pfValue,
                valueColor = pfColor,
                accentColor = pfColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun KpiChip(
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
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
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
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
