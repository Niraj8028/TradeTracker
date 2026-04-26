package com.wallstreet.presentation.analytics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wallstreet.core.util.TradeSummary
import com.wallstreet.ui.theme.DangerRed
import com.wallstreet.ui.theme.DarkSurfaceVariant
import com.wallstreet.ui.theme.LocalBorderColors
import com.wallstreet.ui.theme.SuccessGreen
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
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LongShortCard(summary = summary)
    }
}
