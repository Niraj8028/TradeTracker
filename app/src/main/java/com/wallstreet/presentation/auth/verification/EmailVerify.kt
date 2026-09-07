package com.wallstreet.presentation.auth.verification

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wallstreet.R
import com.wallstreet.presentation.auth.components.AuthPrimaryButton
import com.wallstreet.ui.theme.Gradient
import com.wallstreet.ui.theme.SuccessGreen
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

private fun openEmailApp(context: Context) {
    val intent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_APP_EMAIL)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        context.startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_SENDTO).apply { data = Uri.parse("mailto:") },
                "Open Email App"
            )
        )
    }
}

@Composable
fun EmailVerificationScreen(
    email: String,
    onVerified: () -> Unit,
    onBack: () -> Unit,
    viewModel: EmailVerifyViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val gradient = Gradient.current

    var snackbarIsError by remember { mutableStateOf(true) }
    var resendCooldown by remember { mutableIntStateOf(0) }

    // Poll only while the screen is actually visible.
    LifecycleStartEffect(Unit) {
        viewModel.startPolling()
        onStopOrDispose { viewModel.stopPolling() }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            delay(1200)
            onVerified()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarIsError = true
            val msg = if (it == "NOT_VERIFIED_YET")
                context.getString(R.string.verification_not_verified_yet) else it
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.resendSuccess) {
        if (uiState.resendSuccess) {
            snackbarIsError = false
            snackbarHostState.showSnackbar(context.getString(R.string.verification_email_resent))
            viewModel.clearError()
            resendCooldown = 60
        }
    }

    LaunchedEffect(resendCooldown) {
        if (resendCooldown > 0) {
            delay(1000L)
            resendCooldown--
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "floating")
    val floatY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(tween(1600, easing = EaseInOut), RepeatMode.Reverse),
        label = "floatY"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1000, easing = EaseInOut), RepeatMode.Reverse),
        label = "pulse"
    )
    val successScale by animateFloatAsState(
        targetValue = if (uiState.isSuccess) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "successScale"
    )

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 50.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(
                        shape = RoundedCornerShape(12.dp),
                        containerColor = if (snackbarIsError)
                            MaterialTheme.colorScheme.error else SuccessGreen,
                        contentColor = Color.White
                    ) { Text(data.visuals.message) }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 72.dp, start = 32.dp, end = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (uiState.isSuccess)
                        stringResource(R.string.verification_success_title)
                    else stringResource(R.string.verification_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(14.dp))

                if (uiState.isSuccess) {
                    Text(
                        text = stringResource(R.string.verification_success_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                } else {
                    Text(
                        text = buildAnnotatedString {
                            append(stringResource(R.string.verification_message))
                            append(" ")
                            withStyle(
                                SpanStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            ) { append(email) }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    TextButton(onClick = {
                        viewModel.abandon()
                        onBack()
                    }) {
                        Text(
                            text = stringResource(R.string.verification_wrong_email),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.55f)
                    .align(Alignment.BottomCenter),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 28.dp, vertical = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(120.dp))
                    Spacer(Modifier.weight(1f))

                    if (!uiState.isSuccess) {
                        AuthPrimaryButton(
                            text = stringResource(R.string.verification_ive_verified),
                            onClick = { viewModel.checkNow() },
                            loading = uiState.isChecking,
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { openEmailApp(context) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(50.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.verification_open_email_app),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.verification_no_email),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            when {
                                uiState.isResending -> {
                                    Spacer(Modifier.width(8.dp))
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                resendCooldown > 0 -> {
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = stringResource(R.string.verification_resend) +
                                            " (${resendCooldown}s)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                else -> {
                                    TextButton(
                                        onClick = { viewModel.resendEmail() },
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                            horizontal = 4.dp
                                        )
                                    ) {
                                        Text(
                                            text = stringResource(R.string.verification_resend),
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-300).dp)
                    .zIndex(10f)
                    .size(190.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedVisibility(
                    visible = !uiState.isSuccess,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Image(
                        painter = painterResource(R.drawable.email_send),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .size(160.dp)
                            .offset(y = floatY.dp)
                            .scale(pulseScale)
                    )
                }
                AnimatedVisibility(
                    visible = uiState.isSuccess,
                    enter = fadeIn() + scaleIn(
                        initialScale = 0.5f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ),
                    exit = fadeOut()
                ) {
                    Image(
                        painter = painterResource(R.drawable.mail_validation),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .size(160.dp)
                            .scale(successScale)
                    )
                }
            }
        }
    }
}
