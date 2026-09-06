package com.wallstreet.presentation.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallstreet.core.preferences.CurrencyPreferences
import com.wallstreet.core.preferences.OnboardingPreferences
import com.wallstreet.core.result.Result
import com.wallstreet.domain.model.User
import com.wallstreet.domain.repository.UserRepository
import com.wallstreet.domain.usecase.auth.SendPasswordResetEmailUseCase
import com.wallstreet.domain.usecase.auth.SignInUseCase
import com.wallstreet.domain.usecase.auth.SignInWithGoogleUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class LoginViewModel(
    private val signIn: SignInUseCase,
    private val signInWithGoogle: SignInWithGoogleUseCase,
    private val sendPasswordResetEmail: SendPasswordResetEmailUseCase,
    private val onboardingPreferences: OnboardingPreferences,
    private val currencyPreferences: CurrencyPreferences,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * On sign-in, seed local caches from the account's Firestore doc: skip onboarding if the
     * account already did it (explicit flag or saved roles), and pull the account currency.
     */
    private suspend fun successState(user: User): LoginUiState {
        val uid = user.id
        val accountDone = when (val prefs = userRepository.getAccountPrefs(uid)) {
            is Result.Success -> {
                prefs.data.currencyCode?.let { currencyPreferences.setCurrency(it) }
                prefs.data.onboardingCompleted
            }
            // Read failed (offline / transient). A returning user has almost certainly
            // finished onboarding, and re-running it would overwrite their account currency,
            // so don't force onboarding on a failed read — the cold-start gate re-checks.
            else -> true
        }
        val done = onboardingPreferences.isOnboardingCompleted(uid) || accountDone
        if (done) onboardingPreferences.setOnboardingCompleted(uid)
        return LoginUiState(isSuccess = true, needsOnboarding = !done)
    }

    fun signIn(email: String, password: String) = viewModelScope.launch {
        if (email.isBlank()) {
            _uiState.value = LoginUiState(error = "ERROR_EMAIL_EMPTY")
            return@launch
        }
        if (password.length < 6) {
            _uiState.value = LoginUiState(error = "ERROR_PASSWORD_TOO_SHORT")
            return@launch
        }
        _uiState.value = LoginUiState(isLoading = true)
        when (val r = signIn.invoke(email, password)) {
            is Result.Success -> {
                _uiState.value = successState(r.data)
            }

            is Result.Error -> {
                if (r.message == "EMAIL_NOT_VERIFIED") {
                    _uiState.value = LoginUiState(navigateToOtp = true)
                } else {
                    _uiState.value = LoginUiState(error = r.message)
                }
            }

            Result.Loading -> _uiState.value = LoginUiState(isLoading = true)

        }
    }

    fun signInWithGoogle(idToken: String) = viewModelScope.launch {
        _uiState.value = LoginUiState(isLoading = true)
        _uiState.value = when (val r = signInWithGoogle.invoke(idToken)) {
            is Result.Success -> successState(r.data)
            is Result.Error -> LoginUiState(error = r.message)
            is Result.Loading -> LoginUiState(isLoading = true)
        }
    }

    /** Google account picker returned an unusable result (API failure / missing token). */
    fun onGoogleSignInFailed() {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            error = "Google sign-in failed. Please try again."
        )
    }

    fun forgotPassword(email: String) = viewModelScope.launch {
        if (email.isBlank()) {
            _uiState.value = LoginUiState(error = "ERROR_EMAIL_EMPTY")
            return@launch
        }
        _uiState.value = LoginUiState(isLoading = true)
        when (val r = sendPasswordResetEmail.invoke(email)) {
            is Result.Success -> _uiState.value = LoginUiState(resetEmailSent = true)
            is Result.Error -> _uiState.value = LoginUiState(error = r.message)
            else -> {}
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /** Consume the one-shot navigate-to-OTP event so it cannot re-fire on screen restore. */
    fun resetNavigation() {
        _uiState.value = _uiState.value.copy(navigateToOtp = false)
    }

    /** Consume the reset-email-sent flag after snackbar is shown. */
    fun clearResetEmailSent() {
        _uiState.value = _uiState.value.copy(resetEmailSent = false)
    }
}
