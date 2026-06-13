package com.wallstreet.presentation.log_trade.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.wallstreet.core.util.HapticStyle
import com.wallstreet.core.util.hapticClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wallstreet.domain.model.TrendDirection
import com.wallstreet.ui.theme.DangerRed
import com.wallstreet.ui.theme.PrimaryBlue
import com.wallstreet.ui.theme.SuccessGreen

@Composable
fun TrendDirectionSelector(
    selected: TrendDirection?,
    onSelected: (TrendDirection) -> Unit
) {
    val options = listOf(
        Triple(TrendDirection.UP, "Uptrend", Icons.Default.ArrowUpward),
        Triple(TrendDirection.DOWN, "Downtrend", Icons.Default.ArrowDownward),
        Triple(TrendDirection.SIDEWAYS, "Sideways", Icons.Default.ArrowForward),
    )

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Market Trend",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { (dir, label, icon) ->
                val isSelected = selected == dir
                val accentColor = when (dir) {
                    TrendDirection.UP -> SuccessGreen
                    TrendDirection.DOWN -> DangerRed
                    TrendDirection.SIDEWAYS -> PrimaryBlue
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) accentColor.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.background
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) accentColor
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .hapticClickable(HapticStyle.Light) { onSelected(dir) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) accentColor
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) accentColor
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
