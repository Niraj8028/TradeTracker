package com.wallstreet.presentation.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.wallstreet.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.splash_logo)
    )

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
        speed = 1f
    )

    // Controls whether the text is visible — triggered after Lottie loads
    var textVisible by remember { mutableStateOf(false) }

    // Subtitle fades in slightly after the title
    var subtitleVisible by remember { mutableStateOf(false) }

    // Trigger text animations once composition is loaded
    LaunchedEffect(composition) {
        if (composition != null) {
            delay(400)          // wait for Lottie to start playing
            textVisible = true
            delay(200)          // subtitle staggered 200ms after title
            subtitleVisible = true
        }
    }

    // Navigate away when animation finishes
    LaunchedEffect(progress) {
        if (progress == 1f) {
            delay(400)
            onSplashComplete()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(200.dp)
        )


        // Title — slides up + fades in
        AnimatedVisibility(
            visible = textVisible,
            enter = fadeIn(animationSpec = tween(600, easing = FastOutSlowInEasing)) +
                    slideInVertically(
                        animationSpec = tween(600, easing = FastOutSlowInEasing),
                        initialOffsetY = { it / 2 }   // slides up from 50% below
                    )
        ) {
            Text(
                text = "WallStreet",
                color = Color(0xFF00C087),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Subtitle — fades in only, staggered after title
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
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp,
                letterSpacing = 4.sp,
                fontWeight = FontWeight.Light
            )
        }
    }
}