package com.wallstreet.presentation.analytics.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wallstreet.R
import com.wallstreet.presentation.analytics.AnalyticsViewModel
import com.wallstreet.presentation.analytics.CalenderDay
import com.wallstreet.ui.theme.DangerRed
import com.wallstreet.ui.theme.PrimaryBlue
import com.wallstreet.ui.theme.SuccessGreen
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun Calendar(viewModel: AnalyticsViewModel = koinViewModel()) {
    val initialPage = 500
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { 1000 })
    val scope = rememberCoroutineScope()
    val baseMonth = remember { YearMonth.now() }
    val currentMonth = viewModel.currentMonth

    LaunchedEffect(pagerState.currentPage) {
        val month = baseMonth.plusMonths((pagerState.currentPage - initialPage).toLong())
        if (viewModel.currentMonth != month) viewModel.setMonth(month)
    }

    val monthlyStats = remember(viewModel.calendarDays) {
        val tradingDays = viewModel.calendarDays.filter { it.isCurrentMonth && it.tradeCount > 0 }
        Triple(
            tradingDays.sumOf { it.pnl },
            tradingDays.count { it.pnl > 0 },
            tradingDays.count { it.pnl < 0 }
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                MonthHeader(
                    currentMonth = currentMonth,
                    onPrev = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                    onNext = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } }
                )

                Spacer(modifier = Modifier.height(16.dp))

                DayOfWeekHeader(days = viewModel.days)

                Spacer(modifier = Modifier.height(4.dp))

                HorizontalPager(state = pagerState) {
                    CalendarGrid(days = viewModel.calendarDays)
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(12.dp))

                MonthSummary(monthlyStats)
            }
        }
    }
}

@Composable
private fun MonthHeader(currentMonth: YearMonth, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        NavButton(rotated = true, onClick = onPrev)

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = currentMonth.month
                    .getDisplayName(TextStyle.FULL, Locale.getDefault())
                    .replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = currentMonth.year.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        NavButton(rotated = false, onClick = onNext)
    }
}

@Composable
private fun NavButton(rotated: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.scheveron_arrow),
            contentDescription = null,
            modifier = Modifier
                .size(14.dp)
                .then(if (rotated) Modifier.rotate(180f) else Modifier),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
        )
    }
}

@Composable
private fun DayOfWeekHeader(days: List<String>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        days.forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun CalendarGrid(days: List<CalenderDay>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        days.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    DayCell(day = day, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DayCell(day: CalenderDay, modifier: Modifier = Modifier) {
    val bgColor = when {
        day.isCurrentMonth && day.pnl > 0 -> SuccessGreen.copy(alpha = 0.15f)
        day.isCurrentMonth && day.pnl < 0 -> DangerRed.copy(alpha = 0.15f)
        else -> Color.Transparent
    }
    val onSurface = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .then(
                if (day.isToday)
                    Modifier.border(1.dp, PrimaryBlue, RoundedCornerShape(6.dp))
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (day.date != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = day.date.dayOfMonth.toString(),
                    fontSize = 11.sp,
                    color = when {
                        !day.isCurrentMonth -> onSurface.copy(alpha = 0.2f)
                        day.isToday -> PrimaryBlue
                        else -> onSurface
                    },
                    fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal
                )
                if (day.isCurrentMonth && day.pnl != 0.0) {
                    Text(
                        text = if (day.pnl > 0) "+${day.pnl.toInt()}" else "${day.pnl.toInt()}",
                        fontSize = 7.sp,
                        color = if (day.pnl > 0) SuccessGreen else DangerRed,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthSummary(stats: Triple<Double, Int, Int>) {
    val (totalPnl, winDays, lossDays) = stats
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SummaryItem(
            label = "Monthly P&L",
            value = if (totalPnl >= 0) "+$${totalPnl.toInt()}" else "-$${(-totalPnl).toInt()}",
            color = if (totalPnl >= 0) SuccessGreen else DangerRed
        )
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(32.dp)
                .background(MaterialTheme.colorScheme.outline)
        )
        SummaryItem(label = "Win Days", value = "$winDays", color = SuccessGreen)
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(32.dp)
                .background(MaterialTheme.colorScheme.outline)
        )
        SummaryItem(label = "Loss Days", value = "$lossDays", color = DangerRed)
    }
}

@Composable
private fun SummaryItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
