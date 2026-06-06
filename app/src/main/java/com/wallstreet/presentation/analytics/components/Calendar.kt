package com.wallstreet.presentation.analytics.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.wallstreet.core.util.calculateTotalPnL
import com.wallstreet.core.util.formatPnl
import com.wallstreet.domain.model.CalendarDay
import com.wallstreet.domain.model.Trade
import com.wallstreet.ui.theme.DangerRed
import com.wallstreet.ui.theme.PrimaryBlue
import com.wallstreet.ui.theme.SuccessGreen
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun Calendar(allTrades: List<Trade>) {
    val initialPage = 500
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { 1000 })
    val scope = rememberCoroutineScope()
    val baseMonth = remember { YearMonth.now() }

    // Current display month tracks the settled page so the header + summary
    // update only once the swipe animation finishes — no mid-swipe flicker.
    val currentDisplayMonth by remember {
        derivedStateOf {
            baseMonth.plusMonths((pagerState.currentPage - initialPage).toLong())
        }
    }

    val monthlyStats = remember(pagerState.currentPage, allTrades) {
        val days = buildCalendarDays(currentDisplayMonth, allTrades)
        val tradingDays = days.filter { it.isCurrentMonth && it.tradeCount > 0 }
        Triple(
            tradingDays.sumOf { it.pnl },
            tradingDays.count { it.pnl > 0 },
            tradingDays.count { it.pnl < 0 }
        )
    }

    val daysOfWeek = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            MonthHeader(
                currentMonth = currentDisplayMonth,
                onPrev = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                onNext = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } }
            )

            Spacer(modifier = Modifier.height(16.dp))

            DayOfWeekHeader(days = daysOfWeek)

            Spacer(modifier = Modifier.height(4.dp))

            // Each page independently computes its own month's data — no ViewModel
            // round-trip, so the grid content is ready as soon as the page slides in.
            HorizontalPager(state = pagerState) { page ->
                val pageMonth = remember(page) {
                    baseMonth.plusMonths((page - initialPage).toLong())
                }
                val pageDays = remember(page, allTrades) {
                    buildCalendarDays(pageMonth, allTrades)
                }
                CalendarGrid(days = pageDays)
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(12.dp))

            MonthSummary(monthlyStats)
        }
    }
}

/**
 * Pure computation of a 42-slot calendar grid for [yearMonth] from raw trades.
 * Called per-page so each page renders its own data without an async round-trip.
 */
private fun buildCalendarDays(yearMonth: YearMonth, trades: List<Trade>): List<CalendarDay> {
    val gridSize = 42
    val firstDay = yearMonth.atDay(1)
    val offset = firstDay.dayOfWeek.value % 7
    val daysInMonth = yearMonth.lengthOfMonth()
    val today = LocalDate.now()

    val tradesByDate = trades
        .filter { it.tradeDate > 0 }
        .groupBy {
            Instant.ofEpochMilli(it.tradeDate)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }

    return List(gridSize) { index ->
        when {
            index < offset -> CalendarDay(
                date = null,
                isCurrentMonth = false,
                isToday = false,
                isSelected = false,
                pnl = 0.0,
                tradeCount = 0
            )
            index < offset + daysInMonth -> {
                val date = yearMonth.atDay(index - offset + 1)
                val dayTrades = tradesByDate[date] ?: emptyList()
                CalendarDay(
                    date = date,
                    isCurrentMonth = true,
                    isToday = date == today,
                    isSelected = false,
                    pnl = dayTrades.calculateTotalPnL(),
                    tradeCount = dayTrades.size
                )
            }
            else -> CalendarDay(
                date = null,
                isCurrentMonth = false,
                isToday = false,
                isSelected = false,
                pnl = 0.0,
                tradeCount = 0
            )
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
fun CalendarGrid(days: List<CalendarDay>) {
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
private fun DayCell(day: CalendarDay, modifier: Modifier = Modifier) {
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
        day.date?.let { date ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = date.dayOfMonth.toString(),
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
            value = totalPnl.formatPnl(),
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
