package com.wallstreet.presentation.onboarding

data class OnboardingUiState(
    val stepIndex: Int = 0,
    val selectedRoles: List<String> = emptyList(),
    val selectedCurrency: String = "USD",
    val isFinishing: Boolean = false,
    val finished: Boolean = false,
    val error: String? = null,
)
