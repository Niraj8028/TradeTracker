package com.wallstreet.presentation.auth.verification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@Composable
fun EmailVerificationScreen(
    onVerified: () -> Unit,
    viewModel: EmailVerifyViewModel = koinViewModel()
) {
    val uiState by viewModel._uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onVerified()
    }

    LaunchedEffect(uiState.error, uiState.resendSuccess) {
        if (uiState.error != null) {
            snackbarHostState.showSnackbar(uiState.error!!)
            viewModel.clearError()
        }
        if (uiState.resendSuccess) {
            snackbarHostState.showSnackbar("Verification email resent!")
            viewModel.clearError()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("📧", fontSize = 64.sp)
            Spacer(Modifier.height(24.dp))

            Text(
                "Verify Your Email",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))

            Text(
                "We sent a verification link to your email.\n" +
                        "Click the link in your inbox and we'll\n" +
                        "automatically continue.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(48.dp))

            // Just a spinner — no button needed
            CircularProgressIndicator()

            Spacer(Modifier.height(16.dp))
            Text(
                "Waiting for verification...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))

            TextButton(
                onClick = { viewModel.resendEmail() },
                enabled = !uiState.isResending
            ) {
                if (uiState.isResending) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Resend Verification Link")
                }
            }
        }
    }
}