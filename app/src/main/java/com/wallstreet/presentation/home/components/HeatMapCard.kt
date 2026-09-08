package com.wallstreet.presentation.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.wallstreet.core.util.HapticStyle
import com.wallstreet.core.util.hapticClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import com.wallstreet.ui.theme.LocalCurrencySymbol
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wallstreet.core.preferences.ThemePreferences
import com.wallstreet.core.preferences.ThemeTypes
import com.wallstreet.core.util.formatPnl
import com.wallstreet.domain.model.HeatMapCell
import com.wallstreet.domain.model.HeatMapData
import com.wallstreet.domain.model.HeatType
import com.wallstreet.ui.theme.DangerRed
import com.wallstreet.ui.theme.HeatmapEmpty
import com.wallstreet.ui.theme.HeatmapEmptyDark
import com.wallstreet.ui.theme.SuccessGreen
import com.wallstreet.ui.theme.White
import java.time.format.DateTimeFormatter

private val DAY_LABELS = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")

@Composable
fun HeatMapCard(
    heatMapData: HeatMapData,
    onViewCalender: () -> Unit = {}
) {
    var selectedCell by remember { mutableStateOf<HeatMapCell?>(null) }

    // Compute streak from consecutive active trading days (most recent first)
    val sortedActiveCells = heatMapData.weeks
        .flatMap { it.days }
        .filter { it.type != HeatType.NEUTRAL }
        .sortedByDescending { it.date }

    val winStreak = sortedActiveCells
        .takeWhile { it.type == HeatType.PROFIT }
        .size

    val bestDay = sortedActiveCells.maxByOrNull { it.totalPnl }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(alpha = 0.3f),
                spotColor = Color.Black.copy(alpha = 0.3f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HeatMapHeader(onViewCalender = onViewCalender)
        DayLabelRow()
        WeeksGrid(
            heatMapData = heatMapData,
            selectedCell = selectedCell,
            onCellSelected = { cell ->
                selectedCell = if (selectedCell == cell) null else cell
            }
        )

        // Tap-to-reveal detail strip
        AnimatedVisibility(
            visible = selectedCell != null && selectedCell!!.type != HeatType.NEUTRAL,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            selectedCell?.let { SelectedCellDetail(it) }
        }

        // Win streak + best day row
        if (bestDay != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Win streak chip
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SuccessGreen.copy(alpha = 0.08f))
                        .border(1.dp, SuccessGreen.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    Text(
                        text = "Win streak",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.sp
                    )
                    Text(
                        text = if (winStreak > 0) "$winStreak days" else "None",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (winStreak > 0) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Best day chip
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SuccessGreen.copy(alpha = 0.08f))
                        .border(1.dp, SuccessGreen.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    Text(
                        text = "Best day",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.sp
                    )
                    Text(
                        text = bestDay.totalPnl.formatPnl(LocalCurrencySymbol.current),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = SuccessGreen
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectedCellDetail(cell: HeatMapCell) {
    val accentColor = if (cell.type == HeatType.PROFIT) SuccessGreen else DangerRed
    val formatter = DateTimeFormatter.ofPattern("EEE, MMM d")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(accentColor.copy(alpha = 0.08f))
            .border(1.dp, accentColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = cell.date.format(formatter),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            DetailItem(value = "${cell.noOfTrades}", label = "trades")
            DetailItem(
                value = cell.totalPnl.formatPnl(LocalCurrencySymbol.current),
                label = "P&L",
                valueColor = accentColor
            )
        }
    }
}

@Composable
private fun DetailItem(value: String, label: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 9.sp
        )
    }
}


@Composable
fun DayLabelRow() {
    Row(modifier = Modifier.fillMaxWidth()) {
        DAY_LABELS.forEach { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun WeeksGrid(
    heatMapData: HeatMapData,
    selectedCell: HeatMapCell? = null,
    onCellSelected: (HeatMapCell) -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        heatMapData.weeks.forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val cells = week.days + List(7 - week.days.size) { null }
                cells.forEach { cell ->
                    HeatMapDay(
                        cell = cell,
                        isSelected = cell != null && cell == selectedCell,
                        onSelect = { if (cell != null) onCellSelected(cell) },
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
    isSelected: Boolean = false,
    onSelect: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val themePrefs = ThemePreferences(context)
    val selectedTheme by themePrefs.theme.collectAsState(ThemeTypes.SYSTEM)
    val emptyColor = when (selectedTheme) {
        ThemeTypes.LIGHT -> HeatmapEmpty
        ThemeTypes.DARK -> HeatmapEmptyDark
        ThemeTypes.SYSTEM -> if (isSystemInDarkTheme()) HeatmapEmptyDark else HeatmapEmpty
    }

    val cellColor = when {
        cell == null -> Color.Transparent
        cell.type == HeatType.NEUTRAL -> emptyColor
        cell.type == HeatType.PROFIT -> SuccessGreen.copy(alpha = cell.intensity)
        else -> DangerRed.copy(alpha = cell.intensity)
    }

    val isActive = cell != null && cell.type != HeatType.NEUTRAL

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(5.dp))
            .background(cellColor)
            .then(
                if (isSelected) Modifier.border(1.5.dp, White.copy(alpha = 0.7f), RoundedCornerShape(5.dp))
                else Modifier
            )
            .then(if (isActive) Modifier.hapticClickable(HapticStyle.Light) { onSelect() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        if (cell != null && cell.noOfTrades > 0) {
            Text(
                text = if (cell.noOfTrades > 9) "9+" else "${cell.noOfTrades}",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
        }
    }
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
                text = "Tap a day for details",
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
