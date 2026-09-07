package com.wallstreet.presentation.onboarding.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wallstreet.presentation.auth.components.AuthPrimaryButton
import com.wallstreet.ui.theme.Gradient

/**
 * Shared shell for each onboarding step: app [Gradient] background, an animated segmented
 * progress bar (+ optional back arrow), a title/subtitle block, a flexible [content] slot,
 * and a bottom primary pill CTA with an optional secondary text action.
 */
@Composable
fun OnboardingScaffold(
    stepIndex: Int,
    stepCount: Int,
    title: String,
    subtitle: String,
    primaryText: String,
    onPrimary: () -> Unit,
    modifier: Modifier = Modifier,
    primaryEnabled: Boolean = true,
    primaryLoading: Boolean = false,
    onBack: (() -> Unit)? = null,
    secondaryText: String? = null,
    onSecondary: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Gradient.current)
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(horizontal = 24.dp),
    ) {
        Column(Modifier.fillMaxSize()) {
            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) {
                    IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(stepCount) { i ->
                        val active = i == stepIndex
                        val filled = i <= stepIndex
                        val barWidth by animateDpAsState(
                            targetValue = if (active) 24.dp else 8.dp,
                            label = "stepBarWidth",
                        )
                        Box(
                            Modifier
                                .height(4.dp)
                                .width(barWidth)
                                .clip(CircleShape)
                                .background(
                                    if (filled) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                )
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(20.dp))

            Column(Modifier.weight(1f), content = content)

            Spacer(Modifier.height(12.dp))
            AuthPrimaryButton(
                text = primaryText,
                onClick = onPrimary,
                loading = primaryLoading,
                enabled = primaryEnabled,
            )
            if (secondaryText != null && onSecondary != null) {
                TextButton(
                    onClick = onSecondary,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(secondaryText, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Spacer(Modifier.height(4.dp))
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
