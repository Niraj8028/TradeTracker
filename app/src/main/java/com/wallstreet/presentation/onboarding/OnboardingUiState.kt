package com.wallstreet.presentation.onboarding

data class OnboardingUiState(
    val selectedRoles: List<String> = emptyList(),
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
    val currentMs: Long = 0L,
    val durationMs: Long = 0L
)