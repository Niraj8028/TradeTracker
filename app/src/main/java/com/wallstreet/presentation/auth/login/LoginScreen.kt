package com.wallstreet.presentation.auth.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wallstreet.R
import com.wallstreet.presentation.auth.components.AuthHeader
import com.wallstreet.presentation.auth.components.AuthPrimaryButton
import com.wallstreet.presentation.auth.components.AuthScaffold
import com.wallstreet.presentation.auth.components.AuthSocialButton
import com.wallstreet.presentation.auth.components.AuthTextField
import com.wallstreet.presentation.auth.components.AuthTextLink
import com.wallstreet.presentation.auth.components.rememberGoogleSignInLauncher
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToOtp: (String) -> Unit,
    onNavigateToOnboarding: () -> Unit,
    viewModel: LoginViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    var showForgotDialog by rememberSaveable { mutableStateOf(false) }
    var resetEmail by rememberSaveable { mutableStateOf("") }
    var snackbarIsError by remember { mutableStateOf(true) }

    LaunchedEffect(uiState.navigateToOtp) {
        if (uiState.navigateToOtp) {
            viewModel.resetNavigation()
            onNavigateToOtp(email)
        }
    }
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            if (uiState.needsOnboarding) onNavigateToOnboarding() else onLoginSuccess()
        }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            val message = when (error) {
                "ERROR_EMAIL_ALREADY_IN_USE" -> context.getString(R.string.error_email_already_in_use)
                "ERROR_INVALID_PASSWORD"     -> context.getString(R.string.error_invalid_password)
                "ERROR_USER_NOT_FOUND"       -> context.getString(R.string.error_user_not_found)
                "ERROR_NETWORK_CONNECTION"   -> context.getString(R.string.error_network_connection)
                "EMAIL_NOT_VERIFIED"         -> context.getString(R.string.error_email_not_verified)
                "ERROR_EMAIL_EMPTY"          -> context.getString(R.string.error_email_empty)
                "ERROR_PASSWORD_TOO_SHORT"   -> context.getString(R.string.error_password_too_short)
                else -> error
            }
            snackbarIsError = true
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }
    LaunchedEffect(uiState.resetEmailSent) {
        if (uiState.resetEmailSent) {
            viewModel.clearResetEmailSent()
            showForgotDialog = false
            snackbarIsError = false
            snackbarHostState.showSnackbar(context.getString(R.string.login_reset_email_sent))
        }
    }

    val triggerGoogleSignIn = rememberGoogleSignInLauncher(
        onIdToken = { viewModel.signInWithGoogle(it) },
        onError = { viewModel.onGoogleSignInFailed() },
    )

    if (showForgotDialog) {
        ForgotPasswordDialog(
            email = resetEmail,
            onEmailChange = { resetEmail = it },
            isLoading = uiState.isLoading,
            onDismiss = { if (!uiState.isLoading) showForgotDialog = false },
            onSend = { viewModel.forgotPassword(resetEmail) },
        )
    }

    AuthScaffold(
        snackbarHostState = snackbarHostState,
        snackbarIsError = snackbarIsError,
        header = {
            AuthHeader(
                title = stringResource(R.string.login_title),
                subtitle = stringResource(R.string.login_subtitle),
            )
        },
        sheetContent = {
            AuthTextField(
                value = email,
                onValueChange = { email = it },
                label = stringResource(R.string.auth_email_address),
                placeholder = stringResource(R.string.auth_email_placeholder),
                leadingIcon = Icons.Filled.Email,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
            )
            AuthTextField(
                value = password,
                onValueChange = { password = it },
                label = stringResource(R.string.auth_password),
                placeholder = stringResource(R.string.auth_password_placeholder),
                leadingIcon = Icons.Filled.Lock,
                isPassword = true,
                imeAction = ImeAction.Done,
                onImeAction = {
                    focusManager.clearFocus()
                    viewModel.signIn(email, password)
                },
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = {
                    resetEmail = email
                    showForgotDialog = true
                }) {
                    Text(
                        stringResource(R.string.auth_forgot_password),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            AuthPrimaryButton(
                text = stringResource(R.string.auth_sign_in),
                onClick = {
                    focusManager.clearFocus()
                    viewModel.signIn(email, password)
                },
                loading = uiState.isLoading,
            )

            OrDivider()

            AuthSocialButton(
                text = stringResource(R.string.auth_continue_with_google),
                iconRes = R.drawable.google_icon,
                onClick = triggerGoogleSignIn,
                enabled = !uiState.isLoading,
            )

            AuthTextLink(
                prefix = stringResource(R.string.auth_dont_have_account),
                actionText = stringResource(R.string.auth_sign_up),
                onClick = onNavigateToRegister,
            )
        }
    )
}

@Composable
internal fun OrDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        )
        Text(
            "  ${stringResource(R.string.auth_or)}  ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        )
    }
}

@Composable
private fun ForgotPasswordDialog(
    email: String,
    onEmailChange: (String) -> Unit,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSend: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.login_reset_password_title)) },
        text = {
            Column {
                Text(
                    stringResource(R.string.login_reset_password_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    placeholder = { Text(stringResource(R.string.login_reset_password_placeholder)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { if (email.isNotBlank()) onSend() },
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSend,
                enabled = !isLoading && email.isNotBlank(),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(stringResource(R.string.login_send_reset_link))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text(stringResource(R.string.login_cancel))
            }
        },
    )
}
