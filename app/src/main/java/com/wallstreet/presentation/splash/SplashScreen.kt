package com.wallstreet.presentation.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.wallstreet.R
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: SplashViewModel = koinViewModel ()
) {
    val destination by viewModel.destination.collectAsStateWithLifecycle()


    // React to auth result
    LaunchedEffect(destination) {
        when (destination) {
            is SplashDestination.Home       -> onNavigateToHome()
            is SplashDestination.Onboarding -> onNavigateToOnboarding()
            is SplashDestination.None       -> Unit
        }
    }
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.splash_logo)
    )

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
        speed = 1f
    )
    LaunchedEffect(progress) {
        if (progress == 1f) {
            delay(400)
            viewModel.checkAuthState()
        }
    }
    var textVisible by remember { mutableStateOf(false) }
    var subtitleVisible by remember { mutableStateOf(false) }

    LaunchedEffect(composition) {
        if (composition != null) {
            delay(400)
            textVisible = true
            delay(200)
            subtitleVisible = true
        }
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),  // DarkSurface
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(180.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Title — slides up + fades in
        AnimatedVisibility(
            visible = textVisible,
            enter = fadeIn(animationSpec = tween(600, easing = FastOutSlowInEasing)) +
                    slideInVertically(
                        animationSpec = tween(600, easing = FastOutSlowInEasing),
                        initialOffsetY = { it / 2 }
                    )
        ) {
            Text(
                text = "WallStreet",
                color = MaterialTheme.colorScheme.primary,          // PrimaryBlue
                style = MaterialTheme.typography.headlineLarge,      // 32sp Bold
                letterSpacing = 2.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Subtitle — staggered fade + slide after title
        AnimatedVisibility(
            visible = subtitleVisible,
            enter = fadeIn(animationSpec = tween(800, easing = FastOutSlowInEasing)) +
                    slideInVertically(
                        animationSpec = tween(800, easing = FastOutSlowInEasing),
                        initialOffsetY = { it / 2 }
                    )
        ) {
            Text(
                text = "Trading Journal",
                color = MaterialTheme.colorScheme.onSurfaceVariant,  // DarkTextSecondary
                style = MaterialTheme.typography.bodyMedium,          // 14sp Normal
                letterSpacing = 4.sp,
                fontWeight = FontWeight.Light
            )
        }
    }
}