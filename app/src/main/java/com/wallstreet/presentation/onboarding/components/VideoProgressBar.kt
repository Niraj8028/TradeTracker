package com.wallstreet.presentation.onboarding.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
 
import androidx.compose.foundation.layout.size

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.media3.exoplayer.ExoPlayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoProgressBar(
    progress: Float,
    currentMs: Long,
    durationMs: Long,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    player: ExoPlayer
) {

    Column(
        modifier = modifier.fillMaxWidth()
    ) {

        Slider(
            value = progress,
            onValueChange = { newProgress ->

                val seekPosition =
                    (durationMs * newProgress).toLong()

                player.seekTo(seekPosition)
            },
            modifier = Modifier
                .fillMaxWidth(),

            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor =
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            ),

            thumb = {
                Box(
                    modifier = Modifier
                        .size(15.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            },
            track = { sliderState ->
                SliderDefaults.Track(
                    sliderState = sliderState,
                    modifier = Modifier.height(4.dp),
                    colors = SliderDefaults.colors(
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                )
            }
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = formatDuration(currentMs),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium
            )

            Text(
                text = formatDuration(durationMs),
                color = MaterialTheme.colorScheme.onSurfaceVariant,

                style = MaterialTheme.typography.labelMedium
            )

        }
    }
}

private fun formatDuration(ms: Long): String {

    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    return "%02d:%02d".format(minutes, seconds)
}