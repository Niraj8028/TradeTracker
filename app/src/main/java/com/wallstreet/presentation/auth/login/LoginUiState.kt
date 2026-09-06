package com.wallstreet.presentation.auth.login

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val needsOnboarding: Boolean = false,
    val navigateToOtp: Boolean = false,
    val resetEmailSent: Boolean = false
)
