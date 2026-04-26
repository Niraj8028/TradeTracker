package com.wallstreet.presentation.analytics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.wallstreet.core.util.TradeSummary
import com.wallstreet.core.util.getTradeSummary
import com.wallstreet.domain.model.Trade
import timber.log.Timber

@Composable
fun OverView(summary: TradeSummary) {

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
            .background(color = MaterialTheme.colorScheme.primary)
    ) {
        Text("Overview tab")
    }
}