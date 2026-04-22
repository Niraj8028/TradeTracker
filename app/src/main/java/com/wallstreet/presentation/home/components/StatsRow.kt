package com.wallstreet.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wallstreet.core.util.formatPercent
import com.wallstreet.core.util.formatPnl
import com.wallstreet.domain.model.HomeStats
import com.wallstreet.ui.theme.SuccessGreen

@Composable
fun StatsRow(stats: HomeStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatsCard(
            title = "Total PnL",
            value = stats.totalPnl.formatPnl(),
            modifier = Modifier.weight(1f)
        )
        StatsCard(
            title = "Total Trades",
            value = stats.totalTrades.toString(),
            modifier = Modifier.weight(1f),
        )
        StatsCard(
            title = "WinRate",
            value = stats.winRate.formatPercent(),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun StatsCard(title: String, value: String, modifier: Modifier) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.3f),
                spotColor = Color.Black.copy(alpha = 0.3f)
            )
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = shape
            )
            .padding(vertical = 14.dp, horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
