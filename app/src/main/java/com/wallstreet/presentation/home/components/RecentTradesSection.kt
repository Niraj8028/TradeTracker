package com.wallstreet.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wallstreet.domain.model.RecentTradeItem
import com.wallstreet.domain.model.TradeType
import com.wallstreet.ui.theme.BadgeLongBg
import com.wallstreet.ui.theme.BadgeLongText
import com.wallstreet.ui.theme.BadgeShortBg
import com.wallstreet.ui.theme.BadgeShortText
import com.wallstreet.ui.theme.DangerRed
import com.wallstreet.ui.theme.PrimaryBlue
import com.wallstreet.ui.theme.SuccessGreen
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun RecentTradesSection(
    trades: List<RecentTradeItem>,
    onViewAll: () -> Unit
) {
    val grouped = remember(trades) {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val formatter = DateTimeFormatter.ofPattern("MMM d")

        trades
            .groupBy {
                Instant.ofEpochMilli(it.tradeDate)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }
            .entries
            .sortedByDescending { it.key }
            .map { (date, items) ->
                val label = when (date) {
                    today -> "Today"
                    yesterday -> "Yesterday"
                    else -> date.format(formatter)
                }
                label to items
            }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Trades",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            TextButton(onClick = onViewAll, contentPadding = PaddingValues(0.dp)) {
                Text(
                    text = "See All",
                    color = PrimaryBlue,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        grouped.forEach { (label, dayTrades) ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp, start = 2.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                dayTrades.forEach { trade ->
                    TradeRow(trade)
                }
            }
        }
    }
}

@Composable
private fun TradeRow(trade: RecentTradeItem) {
    val pnl = trade.profitLoss
    val isPnlPositive = pnl >= 0
    val pnlColor = if (isPnlPositive) SuccessGreen else DangerRed
    val pnlText = if (isPnlPositive) "+$${"%.0f".format(pnl)}" else "-$${"%.0f".format(-pnl)}"
    val accentColor = if (trade.tradeType == TradeType.LONG) SuccessGreen else DangerRed

    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), shape)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = trade.symbol.take(2).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = trade.symbol.uppercase(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TradeBadge(tradeType = trade.tradeType)
                }
                Text(
                    text = "${trade.quanity.toInt()} qty  •  ${"%.2f".format(trade.entryPrice)} → ${"%.2f".format(trade.exitPrice ?: 0.0)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(pnlColor.copy(alpha = 0.12f))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = pnlText,
                style = MaterialTheme.typography.labelLarge,
                color = pnlColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TradeBadge(tradeType: TradeType) {
    val isLong = tradeType == TradeType.LONG
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isLong) BadgeLongBg else BadgeShortBg)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = if (isLong) "LONG" else "SHORT",
            style = MaterialTheme.typography.labelSmall,
            color = if (isLong) BadgeLongText else BadgeShortText,
            letterSpacing = 0.5.sp
        )
    }
}
