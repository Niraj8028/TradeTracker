package com.wallstreet.presentation.auth.register

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.focus.FocusDirection
import com.wallstreet.R
import com.wallstreet.presentation.auth.components.AuthHeader
import com.wallstreet.presentation.auth.components.AuthPrimaryButton
import com.wallstreet.presentation.auth.components.AuthScaffold
import com.wallstreet.presentation.auth.components.AuthSocialButton
import com.wallstreet.presentation.auth.components.AuthTextField
import com.wallstreet.presentation.auth.components.AuthTextLink
import com.wallstreet.presentation.auth.components.rememberGoogleSignInLauncher
import com.wallstreet.presentation.auth.login.OrDivider
import org.koin.androidx.compose.koinViewModel

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToOtp: (String) -> Unit,
    onNavigateToOnboarding: () -> Unit,
    viewModel: RegisterViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    val passwordMismatch = confirmPassword.isNotEmpty() && confirmPassword != password

    LaunchedEffect(uiState.navigateToOtp) {
        if (uiState.navigateToOtp) {
            viewModel.resetNavigation()
            onNavigateToOtp(email)
        }
    }
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            if (uiState.needsOnboarding) onNavigateToOnboarding() else onRegisterSuccess()
        }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            val message = when (error) {
                "ERROR_EMAIL_ALREADY_IN_USE"  -> context.getString(R.string.error_email_already_in_use)
                "ERROR_INVALID_PASSWORD"      -> context.getString(R.string.error_invalid_password)
                "ERROR_USER_NOT_FOUND"        -> context.getString(R.string.error_user_not_found)
                "ERROR_NETWORK_CONNECTION"    -> context.getString(R.string.error_network_connection)
                "EMAIL_NOT_VERIFIED"          -> context.getString(R.string.error_email_not_verified)
                "ERROR_NAME_EMPTY"            -> context.getString(R.string.error_name_empty)
                "ERROR_EMAIL_EMPTY"           -> context.getString(R.string.error_email_empty)
                "ERROR_PASSWORD_TOO_SHORT"    -> context.getString(R.string.error_password_too_short)
                "ERROR_PASSWORDS_DO_NOT_MATCH" -> context.getString(R.string.error_passwords_do_not_match)
                else -> error
            }
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    val triggerGoogleSignIn = rememberGoogleSignInLauncher(
        onIdToken = { viewModel.signUpWithGoogle(it) },
        onError = { viewModel.onGoogleSignInFailed() },
    )

    val submit = {
        focusManager.clearFocus()
        viewModel.signUp(fullName, email, password, confirmPassword)
    }

    AuthScaffold(
        snackbarHostState = snackbarHostState,
        snackbarIsError = true,
        sheetHeightFraction = 0.8f,
        header = {
            AuthHeader(
                title = stringResource(R.string.register_title_create) +
                    stringResource(R.string.register_title_account),
                subtitle = stringResource(R.string.register_subtitle),
                showLogo = false,
            )
        },
        sheetContent = {
            AuthTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = stringResource(R.string.register_full_name),
                placeholder = stringResource(R.string.register_full_name_placeholder),
                leadingIcon = Icons.Filled.Person,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
                onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
            )
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
                imeAction = ImeAction.Next,
                onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
            )
            AuthTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = stringResource(R.string.register_confirm_password),
                placeholder = stringResource(R.string.auth_password_placeholder),
                leadingIcon = Icons.Filled.LockOpen,
                isPassword = true,
                isError = passwordMismatch,
                errorText = stringResource(R.string.error_passwords_do_not_match),
                imeAction = ImeAction.Done,
                onImeAction = { submit() },
            )

            AuthPrimaryButton(
                text = stringResource(R.string.register_button),
                onClick = { submit() },
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
                prefix = stringResource(R.string.auth_already_have_account),
                actionText = stringResource(R.string.auth_sign_in),
                onClick = onNavigateToLogin,
            )
        }
    )
}
