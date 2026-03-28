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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
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
import com.wallstreet.ui.theme.PrimaryBlueLight
import com.wallstreet.ui.theme.SuccessGreen

@Composable
fun RecentTradesSection(
    trades: List<RecentTradeItem>,
    onViewAll: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Recent Trades",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            TextButton(
                onClick = onViewAll,
                contentPadding = PaddingValues(0.dp)) {
                Text("See All",
                    color = PrimaryBlue,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
        trades.forEach { trade ->
            TradeRow(trade)
        }

    }
}

@Composable
private fun TradeRow(trade: RecentTradeItem) {
    val isPnlPositive = (trade.profitLoss ?: 0.0) >= 0
    val pnlColor = if (isPnlPositive) SuccessGreen else DangerRed
    val pnlSign = if (isPnlPositive) "+" else ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(4.dp),
                ambientColor = Color.Black.copy(alpha = 0.3f),
                spotColor = Color.Black.copy(alpha = 0.3f)
            )
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ){
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
//            Box(
//                modifier = Modifier
//                    .size(42.dp)
//                    .clip(RoundedCornerShape(10.dp))
//                    .background(MaterialTheme.colorScheme.surfaceVariant),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(
//                    text = trade.symbol.take(3).uppercase(),
//                    style = MaterialTheme.typography.labelMedium,
//                    color = PrimaryBlueLight,
//                    fontWeight = FontWeight.Bold
//                )
//            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Symbol + badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = trade.symbol.uppercase(),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TradeBadge(tradeType = trade.tradeType)
                }
                // Quantity • Entry price
                Text(
                    text = "${trade.quanity.toInt()} qty  •  ${"%.2f".format(trade.entryPrice)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "$pnlSign$${"%.2f".format(trade.profitLoss ?: 0.0)}",
                style = MaterialTheme.typography.titleSmall,
                color = pnlColor
            )
            Text(
                text = "exit at ${"%.2f".format(trade.exitPrice ?: 0.0)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TradeBadge(tradeType: TradeType) {
    val isLong = tradeType == TradeType.LONG
    val bgColor = if (isLong) BadgeLongBg else BadgeShortBg
    val textColor = if (isLong) BadgeLongText else BadgeShortText

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = if (isLong) "LONG" else "SHORT",
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            letterSpacing = 0.5.sp
        )
    }
}
