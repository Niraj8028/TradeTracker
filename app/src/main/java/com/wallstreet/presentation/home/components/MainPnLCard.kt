package com.wallstreet.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wallstreet.domain.model.HomeStats
import com.wallstreet.domain.model.TimePeriod
import com.wallstreet.ui.theme.DangerRed
import com.wallstreet.ui.theme.PrimaryBlue
import com.wallstreet.ui.theme.SuccessGreen
import com.wallstreet.ui.theme.White

@Composable
fun MainPnLCard(
    stats: HomeStats,
    onPeriodSelected: (TimePeriod) -> Unit,
    selectedPeriod: TimePeriod
) {
    val isPnlPositive = stats.totalPnl >= 0
    val pnlColor = if (isPnlPositive) SuccessGreen else DangerRed
    val pnlText = if (isPnlPositive)
        "+$${"%.2f".format(stats.totalPnl)}"
    else
        "-$${"%.2f".format(-stats.totalPnl)}"

    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = shape,
                ambientColor = Color.Black.copy(0.3f),
                spotColor = Color.Black.copy(0.3f)
            )
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), shape)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOTAL P&L",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(pnlColor.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${stats.totalWinningTrades}W · ${stats.totalLosingTrades}L",
                        style = MaterialTheme.typography.labelSmall,
                        color = pnlColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = pnlText,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = pnlColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${stats.totalTrades} trades this period",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

        PeriodSelector(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            selectedPeriod = selectedPeriod,
            onPeriodSelected = onPeriodSelected
        )
    }
}

@Composable
fun PeriodSelector(
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        TimePeriod.entries.forEach { period ->
            PeriodChip(
                label = period.label,
                isSelected = period == selectedPeriod,
                onClick = { onPeriodSelected(period) }
            )
        }
    }
}

@Composable
fun PeriodChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
