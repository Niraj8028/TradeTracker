package com.wallstreet.presentation.home.components

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.wallstreet.ui.theme.LocalCurrencySymbol
import com.wallstreet.ui.theme.PrimaryBlue
import com.wallstreet.ui.theme.SuccessGreen

@Composable
fun StatsRow(stats: HomeStats) {
    val symbol = LocalCurrencySymbol.current
    val winRateColor = when {
        stats.winRate >= 60 -> SuccessGreen
        stats.winRate < 40  -> DangerRed
        else                -> PrimaryBlue
    }
    val rrColor = when {
        stats.riskRewardRatio >= 2.0 -> SuccessGreen
        stats.riskRewardRatio >= 1.0 -> PrimaryBlue
        else                         -> DangerRed
    }
    val avgWinText = "+$symbol${"%.0f".format(stats.avgProfit)}"
    val avgLossText = "$symbol${"%.0f".format(stats.avgLoss)}"
    val rrText = "${"%.1f".format(stats.riskRewardRatio)}x"
    val winRateText = "${"%.1f".format(stats.winRate)}%"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                label = "Win Rate",
                value = winRateText,
                valueColor = winRateColor,
                accentColor = winRateColor
            )
            StatCard(
                label = "Avg Loss",
                value = avgLossText,
                valueColor = DangerRed,
                accentColor = DangerRed
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                label = "Avg Win",
                value = avgWinText,
                valueColor = SuccessGreen,
                accentColor = SuccessGreen
            )
            StatCard(
                label = "Risk / Reward",
                value = rrText,
                valueColor = rrColor,
                accentColor = rrColor
            )
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    valueColor: Color,
    accentColor: Color
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.2f),
                spotColor = Color.Black.copy(alpha = 0.2f)
            )
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), shape)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(accentColor.copy(alpha = 0.7f))
        )
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
