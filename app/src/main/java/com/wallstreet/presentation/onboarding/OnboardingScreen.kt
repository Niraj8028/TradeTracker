package com.wallstreet.presentation.onboarding

import android.media.browse.MediaBrowser
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import com.wallstreet.R
import com.wallstreet.presentation.onboarding.components.PageOne
import com.wallstreet.presentation.onboarding.components.PageTwo
import com.wallstreet.ui.theme.Gradient
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import kotlin.math.absoluteValue


@OptIn(UnstableApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit, viewModel: OnboardingViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { 2 })

    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.Builder()
                .setUri(RawResourceDataSource.buildRawResourceUri(R.raw.tutorial))
                .setMimeType(MimeTypes.VIDEO_WEBM)
                .build()
            setMediaItem(mediaItem)
            prepare()
            repeatMode = Player.REPEAT_MODE_ONE
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            player.release()
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage == 1) {
            player.play()
            viewModel.onPlayingChanged(true)
        } else {
            player.pause()
            viewModel.onPlayingChanged(false)
        }
    }

    fun onNext() {
        scope.launch { pagerState.animateScrollToPage(1) }
    }

    fun onBack() {
        scope.launch { pagerState.animateScrollToPage(0) }
    }

    fun onFinished() {
        viewModel.onFinish()
        onFinish()
//        Timber.d("on press finished called")
    }

    fun onContinue() {
        if (pagerState.currentPage == 0) {
            onNext()
        } else (
                onFinished()
                )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gradient.current)
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(horizontal = 24.dp),
    ) {
        Column() {
            Spacer(modifier = Modifier.height(10.dp))

            //indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(2) { index ->
                        val activeIndex = index == pagerState.currentPage

                        Box(
                            modifier = Modifier
                                .height(4.dp)
                                .width(if (activeIndex) 24.dp else 8.dp)
                                .clip(CircleShape)
                                .background(if (activeIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
                        )
                    }

                }
//                Text("Next", modifier = Modifier.clickable(onClick = { onContinue() }))
            }
            //pager view
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 0,
                modifier = Modifier.weight(1f)
            ) { page ->

                val pageOffset = (
                        (pagerState.currentPage - page) +
                                pagerState.currentPageOffsetFraction
                        ).absoluteValue

                Box(
                    modifier = Modifier.graphicsLayer {

                        // alpha
                        alpha = lerp(
                            start = 0.5f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )

                        // scale
                        val scale = lerp(
                            start = 0.9f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )

                        scaleX = scale
                        scaleY = scale

                        // translation
                        translationX = pageOffset * 80f
                    }
                ) {

                    when (page) {

                        0 -> PageOne(
                            selectedRoles = uiState.selectedRoles,
                            onUserTypeSelected = viewModel::onUserTypeSelected
                        )

                        1 -> PageTwo(
                            isPlaying = uiState.isPlaying,
                            progress = uiState.progress,
                            currentMs = uiState.currentMs,
                            durationMs = uiState.durationMs,
                            onProgressChanged = viewModel::onProgressChanged,
                            onPlayingChanged = viewModel::onPlayingChanged,
                            player = player,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            //Continue button
            Button(
                onClick = { onContinue() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    "Continue",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Spacer(modifier = Modifier.height(44.dp))


        }


    }
}