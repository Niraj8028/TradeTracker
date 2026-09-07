package com.wallstreet.presentation.onboarding.components

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wallstreet.core.util.HapticStyle
import com.wallstreet.core.util.hapticClickable

private fun roleIcon(role: String): ImageVector = when (role.lowercase()) {
    "forex" -> Icons.Filled.CurrencyExchange
    "options" -> Icons.Filled.Tune
    "crypto" -> Icons.Filled.CurrencyBitcoin
    "stocks" -> Icons.Filled.ShowChart
    "futures" -> Icons.Filled.Timeline
    "swing" -> Icons.Filled.SwapVert
    "scalping" -> Icons.Filled.Bolt
    "intraday" -> Icons.Filled.Today
    else -> Icons.Filled.CandlestickChart
}

/**
 * Selectable trader-role tile on the app card recipe (surfaceVariant fill, hairline border,
 * 16dp radius). A tinted icon chip + name; selection adds a blue tint, border, and check badge.
 */
@Composable
fun RoleTile(
    role: String,
    imageRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(124.dp)
            .clip(shape)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = shape,
            )
            .hapticClickable(HapticStyle.Medium) { onClick() }
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = roleIcon(role),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp),
                )
            }
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Text(
            text = role,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
