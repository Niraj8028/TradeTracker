package com.wallstreet.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.wallstreet.domain.model.HeatMapCell
import com.wallstreet.domain.model.HeatMapData
import com.wallstreet.domain.model.HeatType
import com.wallstreet.ui.theme.DangerRed
import com.wallstreet.ui.theme.SuccessGreen

private val DAY_LABELS = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")

@Composable
fun HeatMapCard(
    heatMapData: HeatMapData,
    onViewCalender: () -> Unit = {}
    ) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(alpha = 0.3f),
                spotColor = Color.Black.copy(alpha = 0.3f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.background)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HeatMapHeader(onViewCalender = onViewCalender)
        DayLabelRow()
        WeeksGrid(heatMapData)
    }
}

@Composable
fun DayLabelRow() {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        DAY_LABELS.forEach { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}


@Composable
fun WeeksGrid(heatMapData: HeatMapData) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        heatMapData.weeks.forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val cells = week.days + List(7 - week.days.size) { null }
                 cells.forEach { cell ->
                    HeatMapDay(
                        cell = cell,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun HeatMapDay(
    cell: HeatMapCell?,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val cellColour = when {
        cell == null -> Color.Transparent
        cell.type == HeatType.NEUTRAL -> MaterialTheme.colorScheme.surfaceVariant
        cell.type == HeatType.PROFIT -> SuccessGreen.copy(alpha = cell.intensity)
        else -> DangerRed.copy(alpha = cell.intensity)
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(5.dp))
            .background(cellColour)
    )
}

@Composable
fun HeatMapHeader(onViewCalender: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Heatmap",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Daily win / loss",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendDot(color = SuccessGreen, label = "Profit")
            LegendDot(color = DangerRed, label = "Loss")
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}




