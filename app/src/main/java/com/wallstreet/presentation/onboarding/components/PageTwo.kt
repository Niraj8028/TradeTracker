package com.wallstreet.presentation.onboarding.components

import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import com.wallstreet.core.util.HapticStyle
import com.wallstreet.core.util.hapticClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.wallstreet.R
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun PageTwo(
    player: ExoPlayer,
    isPlaying: Boolean,
    progress: Float,
    currentMs: Long,
    durationMs: Long,
    onProgressChanged: (Float, Long, Long) -> Unit,
    onPlayingChanged: (Boolean) -> Unit
) {
    var showOverlay by remember {
        mutableStateOf(false)
    }
    LaunchedEffect(showOverlay) {

        if (showOverlay) {

            delay(1000)

            showOverlay = false
        }
    }
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                onPlayingChanged(playing)
            }
        }
        player.addListener(listener)

        onDispose {
            player.removeListener(listener)
        }
    }

    LaunchedEffect(player) {

        while (true) {
            val dur = player.duration.takeIf { it > 0L } ?: 0L
            val pos = player.currentPosition.coerceIn(0L, dur.coerceAtLeast(1L))
            onProgressChanged(
                if (dur > 0L) pos.toFloat() / dur.toFloat() else 0f,
                pos,
                dur
            )
            delay(200)
        }
    }


    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(21.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.7f)
                .clip(RoundedCornerShape(24.dp))
                .hapticClickable(HapticStyle.Light) {
                    if (isPlaying) {
                        player.pause()
                    } else player.play()
                    showOverlay = true

                },
            contentAlignment = Alignment.Center
        ) {

            AndroidView(
                factory = { ctx ->
                    val view = android.view.LayoutInflater.from(ctx).inflate(R.layout.view_player_texture, null) as PlayerView
                    view.apply {
                        this.player = player
                        useController = false
                        clipToOutline = true
                        resizeMode =
                            AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    }
                },
                update = { view ->
                    view.player = player
                },
                modifier = Modifier.fillMaxSize()
            )

            this@Column.AnimatedVisibility(
                visible = showOverlay,
                enter = scaleIn(
                    initialScale = 0.7f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(animationSpec = tween(150)),
                exit = scaleOut(
                    targetScale = 1.1f,
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (isPlaying) R.drawable.pause else R.drawable.play
                            ),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

        }
        Spacer(modifier = Modifier.height(22.dp))

        Column() {
            Text(
                text = stringResource(R.string.onboarding_page_two_title),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.onboarding_page_two_subtitle),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(20.dp))
            VideoProgressBar(
                player = player,
                progress = progress,
                currentMs = currentMs,
                durationMs = durationMs,
                onSeek = { newProgress ->

                    val seekPosition =
                        (durationMs * newProgress).toLong()

                    player.seekTo(seekPosition)
                },
                modifier = Modifier
            )
        }

    }
}