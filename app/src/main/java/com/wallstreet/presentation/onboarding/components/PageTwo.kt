package com.wallstreet.presentation.onboarding.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay

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


    Column() {
        Box() {

            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx)
                },
                update = { view -> view.player = player },
                modifier = Modifier.fillMaxSize()
            )

        }
        Row() { }

    }
}