package com.wallstreet.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.launch
import com.wallstreet.core.util.HapticStyle
import com.wallstreet.core.util.hapticClickable
import com.wallstreet.navigation.BottomNavItem
import com.wallstreet.ui.theme.PrimaryBlue
import com.wallstreet.ui.theme.White

@Composable
fun AppBottomBar(
    currentKey: NavKey,
    onItemClick: (NavKey) -> Unit,
    onFabClick: () -> Unit
) {
    val items = BottomNavItem.items

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(0.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.take(2).forEach { item ->
                NavIcon(
                    item = item,
                    isSelected = currentKey == item.key,
                    onClick = { onItemClick(item.key) }
                )
            }

            Box(
                modifier = Modifier.padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                PulsingFab(onClick = onFabClick)
            }

            items.drop(2).forEach { item ->
                NavIcon(
                    item = item,
                    isSelected = currentKey == item.key,
                    onClick = { onItemClick(item.key) }
                )
            }
        }
    }
}

@Composable
private fun PulsingFab(onClick: () -> Unit) {
    // Pulse a few times to draw attention, then settle — no continuous distraction.
    val ringScale = remember { Animatable(1f) }
    val ringAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        repeat(3) {
            ringScale.snapTo(1f)
            ringAlpha.snapTo(0.4f)
            launch {
                ringScale.animateTo(1.65f, tween(1400, easing = LinearOutSlowInEasing))
            }
            ringAlpha.animateTo(0f, tween(1400, easing = LinearEasing))
        }
        ringAlpha.snapTo(0f)
    }

    Box(
        modifier = Modifier
            .size(50.dp)
            .drawBehind {
                drawCircle(
                    color = PrimaryBlue.copy(alpha = ringAlpha.value),
                    radius = size.minDimension / 2 * ringScale.value
                )
            }
            .shadow(
                elevation = 6.dp,
                shape = CircleShape,
                ambientColor = PrimaryBlue.copy(alpha = 0.4f),
                spotColor = PrimaryBlue.copy(alpha = 0.4f)
            )
            .clip(CircleShape)
            .background(PrimaryBlue)
            .hapticClickable(HapticStyle.Medium) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Log Trade",
            tint = White,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun NavIcon(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val iconTint by animateColorAsState(
        targetValue = if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        animationSpec = tween(200),
        label = "icon_tint"
    )

    Column(
        modifier = Modifier
            .hapticClickable(HapticStyle.Light) { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = stringResource(item.label),
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = stringResource(item.label),
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = iconTint
        )
    }
}
