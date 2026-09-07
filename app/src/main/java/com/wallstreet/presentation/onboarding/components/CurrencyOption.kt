package com.wallstreet.presentation.onboarding.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wallstreet.core.util.HapticStyle
import com.wallstreet.core.util.hapticClickable

/** Single-select currency row: symbol chip + code/name + radio-style trailing indicator. */
@Composable
fun CurrencyOption(
    code: String,
    symbol: String,
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
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
            .hapticClickable(HapticStyle.Light) { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = symbol,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = code,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
        )
    }
}
