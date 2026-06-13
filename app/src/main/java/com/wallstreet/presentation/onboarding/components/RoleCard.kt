package com.wallstreet.presentation.onboarding.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.wallstreet.core.util.HapticStyle
import com.wallstreet.core.util.hapticClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun RoleCard(
    role: String,
    description: String,
    imageRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(20.dp)

    Box(
        modifier = Modifier
            .height(96.dp)
            .clip(shape)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) primaryColor else Color.Transparent,
                shape = shape
            )
            .hapticClickable(HapticStyle.Medium) { onClick() },
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        // Gradient overlay — more opaque at bottom for text legibility
        val overlayBrush = if (isSelected) {
            Brush.verticalGradient(
                listOf(
                    primaryColor.copy(alpha = 0.35f),
                    primaryColor.copy(alpha = 0.70f)
                )
            )
        } else {
            Brush.verticalGradient(
                listOf(
                    Color.Black.copy(alpha = 0.15f),
                    Color.Black.copy(alpha = 0.65f)
                )
            )
        }
        Box(modifier = Modifier.matchParentSize().background(overlayBrush))

        // Role name + description at bottom-left
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 10.dp, bottom = 8.dp, end = 8.dp)
        ) {
            Text(
                role,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.height(1.dp))
            Text(
                description,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.75f)
            )
        }

        // Animated checkmark badge at top-right when selected
        AnimatedVisibility(
            visible = isSelected,
            enter = scaleIn(
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) + fadeIn(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(7.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(primaryColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}
