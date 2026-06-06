package com.wallstreet.presentation.analytics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wallstreet.domain.model.CalendarDay
import com.wallstreet.domain.model.DayPerformance
import com.wallstreet.domain.model.OverviewStats
import com.wallstreet.domain.model.RecentTradeItem
import com.wallstreet.domain.model.TradeSummary
import java.time.YearMonth

@Composable
fun OverView(
    summary: TradeSummary,
    overviewStats: OverviewStats,
    dayPerformance: DayPerformance,
    recentTrades: List<RecentTradeItem>,
    calendarDays: List<CalendarDay>,
    currentMonth: YearMonth,
    onNextMonth: () -> Unit,
    onPrevMonth: () -> Unit,
    onSetMonth: (YearMonth) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OverviewKpiStrip(stats = overviewStats)
        LongShortCard(summary = summary)
        if (summary.totalTrades > 0) {
            LongShortInsightsCard(summary = summary)
        }
        DayWisePerformance(dayPerformance)
        Calendar(
            calendarDays = calendarDays,
            currentMonth = currentMonth,
            onNextMonth = onNextMonth,
            onPrevMonth = onPrevMonth,
            onSetMonth = onSetMonth
        )
    }
}
