package com.wallstreet.presentation.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wallstreet.core.preferences.OnboardingPreferences
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val description: String,
    val emoji: String
)

private val pages = listOf(
    OnboardingPage(
        title = "Track Every Trade",
        description = "Log your trades with entry, exit, P&L and strategy — all in one place.",
        emoji = "📈"
    ),
    OnboardingPage(
        title = "Analyse Your Mistakes",
        description = "Spot patterns like FOMO, revenge trades and over-leveraging before they cost you more.",
        emoji = "🔍"
    ),
    OnboardingPage(
        title = "Build Better Strategies",
        description = "Compare strategies by win rate, profit factor and expected value.",
        emoji = "🧠"
    ),
    OnboardingPage(
        title = "Stay Consistent",
        description = "Your equity curve, drawdown and monthly heatmap in one dashboard.",
        emoji = "🎯"
    )
)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit, onboardingPreferences: OnboardingPreferences
) {
    val scope = rememberCoroutineScope()
    var currentPage by remember { mutableIntStateOf(0) }
    val isLast = currentPage == pages.lastIndex
    val finish = {
        scope.launch { onboardingPreferences.setOnboardingCompleted() }
        onFinish()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)  // DarkSurface
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Skip button top-right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = finish) {
                Text(
                    text = "Skip",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,  // DarkTextSecondary
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Animated page content
        AnimatedContent(
            targetState = currentPage,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInHorizontally { it } + fadeIn() togetherWith
                            slideOutHorizontally { -it } + fadeOut()
                } else {
                    slideInHorizontally { -it } + fadeIn() togetherWith
                            slideOutHorizontally { it } + fadeOut()
                }
            },
            label = "onboarding_page"
        ) { index ->
            val p = pages[index]
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = p.emoji,
                    fontSize = 80.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = p.title,
                    color = MaterialTheme.colorScheme.onBackground,     // DarkTextPrimary
                    style = MaterialTheme.typography.headlineSmall,      // 24sp SemiBold
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = p.description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,  // DarkTextSecondary
                    style = MaterialTheme.typography.bodyMedium,          // 14sp Normal
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Dot indicators
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            pages.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .size(if (index == currentPage) 10.dp else 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == currentPage) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline               // BorderPrimary
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Prev / Next buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentPage > 0) {
                Button(
                    onClick = { currentPage-- },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,  // DarkSurfaceVariant
                        contentColor = MaterialTheme.colorScheme.onSurface          // DarkTextPrimary
                    )
                ) {
                    Text(
                        text = "Prev",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Button(
                onClick = { if (isLast) finish() else currentPage++ },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,    // PrimaryBlue
                    contentColor = MaterialTheme.colorScheme.onPrimary     // White
                )
            ) {
                Text(
                    text = if (isLast) "Get Started" else "Next",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}