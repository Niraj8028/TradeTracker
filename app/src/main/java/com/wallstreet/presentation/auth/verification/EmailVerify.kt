package com.wallstreet.presentation.auth.verification

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wallstreet.R
import com.wallstreet.ui.theme.Gradient
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
                Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:")
                },
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

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            kotlinx.coroutines.delay(1200)
            onVerified()
        }
    }

    LaunchedEffect(uiState.error, uiState.resendSuccess) {

        if (uiState.error != null) {
            snackbarHostState.showSnackbar(uiState.error!!)
            viewModel.clearError()
        }

        if (uiState.resendSuccess) {
            snackbarHostState.showSnackbar(
                context.getString(R.string.verification_email_resent)
            )
            viewModel.clearError()
        }
    }

    // Floating animation
    val infiniteTransition = rememberInfiniteTransition(label = "floating")

    val floatY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1600,
                easing = EaseInOut
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = EaseInOut
            ),
            repeatMode = RepeatMode.Reverse
        ),
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
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        snackbarHost = {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 50.dp),
                contentAlignment = Alignment.TopCenter
            ) {

                SnackbarHost(
                    hostState = snackbarHostState
                ) { data ->

                    Snackbar(
                        shape = RoundedCornerShape(14.dp),
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ) {
                        Text(data.visuals.message)
                    }
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

            // =========================================================
            // TOP CONTENT
            // =========================================================

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 72.dp,
                        start = 32.dp,
                        end = 32.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = if (uiState.isSuccess) {
                        stringResource(R.string.verification_success_title)
                    } else {
                        stringResource(R.string.verification_title)
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

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
                            ) {
                                append(email)
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }

                if (!uiState.isSuccess) {
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

            // =========================================================
            // BOTTOM CARD
            // =========================================================

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.55f)
                    .align(Alignment.BottomCenter),
                shape = RoundedCornerShape(
                    topStart = 32.dp,
                    topEnd = 32.dp
                ),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            horizontal = 28.dp,
                            vertical = 36.dp
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Spacer(modifier = Modifier.height(120.dp))

                    Spacer(modifier = Modifier.weight(1f))

                    if (!uiState.isSuccess) {

                        Button(
                            onClick = {
                                openEmailApp(context)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {

                            Text(
                                text = stringResource(
                                    R.string.verification_open_email_app
                                ),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                text = stringResource(
                                    R.string.verification_no_email
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (uiState.isResending) {

                                Spacer(modifier = Modifier.width(8.dp))

                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )

                            } else {

                                TextButton(
                                    onClick = {
                                        viewModel.resendEmail()
                                    },
                                    contentPadding = PaddingValues(
                                        horizontal = 4.dp
                                    )
                                ) {

                                    Text(
                                        buildAnnotatedString {
                                            withStyle(
                                                SpanStyle(
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                            ) {
                                                append(
                                                    stringResource(
                                                        R.string.verification_resend
                                                    )
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================
            // FLOATING SVG
            // =========================================================

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
                        colorFilter = ColorFilter.tint(
                            MaterialTheme.colorScheme.primary
                        ),
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
                        colorFilter = ColorFilter.tint(
                            MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .size(160.dp)
                            .scale(successScale)
                    )
                }
            }
        }
    }
}