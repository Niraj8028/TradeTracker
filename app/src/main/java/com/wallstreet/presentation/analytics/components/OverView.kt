package com.wallstreet.presentation.analytics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wallstreet.core.util.DayPerformance
import com.wallstreet.core.util.TradeSummary
import timber.log.Timber

@Composable
fun OverView(summary: TradeSummary, dayPerformance: DayPerformance) {

    Timber.d(
        """
    Trade Summary:

    LONG:
      Count       = ${summary.long.count}
      PnL         = ${summary.long.pnl}
      Win Rate    = ${summary.long.winRate}%
      Percentage  = ${summary.long.percentage}%

    SHORT:
      Count       = ${summary.short.count}
      PnL         = ${summary.short.pnl}
      Win Rate    = ${summary.short.winRate}%
      Percentage  = ${summary.short.percentage}%
    """.trimIndent()
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LongShortCard(summary = summary)
        DayWisePerformance(dayPerformance)
    }
}
