package com.wallstreet.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wallstreet.domain.model.HeatMapCell
import com.wallstreet.domain.model.HeatMapData
import com.wallstreet.domain.model.HeatType
import com.wallstreet.ui.theme.DangerRed
import com.wallstreet.ui.theme.DarkSurface
import com.wallstreet.ui.theme.DarkTextTertiary
import com.wallstreet.ui.theme.SuccessGreen

private val DAY_LABELS = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")

@Composable
fun HeatMapCard(
    heatMapData: HeatMapData,
    onViewCalender: () -> Unit = {}
    ) {
    Column(
        modifier = Modifier.fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
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
                color = DarkTextTertiary,
                fontSize = 9.sp,
                modifier = Modifier.weight(1f)
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
    val cellColour = when {
        cell == null -> Color.Transparent
        cell.type == HeatType.NEUTRAL -> Color(0xFF1A2332)
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

}




