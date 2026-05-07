package com.wallstreet.presentation.onboarding.components

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
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
        ) {

            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {

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
        }
        Column() {
            Text(
                text = "Watch the 60-second tour",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Discover how Trade Coach helps you journal smarter and trade better.",
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