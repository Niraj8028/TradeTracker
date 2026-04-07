package com.wallstreet.presentation.log_trade.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PnlPreviewCard(pnl: Double) {
    val isProfit = pnl >= 0
    val bgColor =
        if (isProfit)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        else
            MaterialTheme.colorScheme.error.copy(alpha = 0.15f)

    val textColor =
        if (isProfit)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.error
    val label = if (isProfit) "ESTIMATED PROFIT" else "ESTIMATED LOSS"
    val sign = if (isProfit) "+" else ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = textColor.copy(alpha = 0.8f),
            letterSpacing = 0.8.sp
        )
        Text(
            text = "$sign$${"%.2f".format(pnl)}",
            style = MaterialTheme.typography.titleMedium,
            color = textColor
        )
    }
}