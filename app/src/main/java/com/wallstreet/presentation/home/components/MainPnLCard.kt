package com.wallstreet.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wallstreet.core.util.formatPnl
import com.wallstreet.domain.model.HomeStats
import com.wallstreet.ui.theme.BadgeLongBg
import com.wallstreet.ui.theme.BadgeShortBg
import com.wallstreet.ui.theme.DangerRed
import com.wallstreet.ui.theme.SuccessGreen

@Composable
fun MainPnLCard(stats: HomeStats) {
    val isPnlPositive = stats.totalPnl >= 0
    val pnlColor = if (isPnlPositive) SuccessGreen else DangerRed
    val pnlBadgeBg = if (isPnlPositive) BadgeLongBg else BadgeShortBg
    Column(

        modifier = Modifier.fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(4.dp),
                ambientColor = Color.Black.copy(0.3f),
                spotColor = Color.Black.copy(0.3f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "TOTAL P&L",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stats.totalPnl.formatPnl(),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(pnlBadgeBg)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stats.avgProfit.formatPnl(),
                    style = MaterialTheme.typography.labelMedium,
                    color = pnlColor
                )
            }
        }
        Text(
            text = "UPDATED JUST NOW",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.8.sp
        )
    }
}