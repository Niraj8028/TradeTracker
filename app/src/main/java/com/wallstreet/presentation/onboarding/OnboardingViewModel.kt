package com.wallstreet.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallstreet.core.preferences.OnboardingPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val onboardingPreferences: OnboardingPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onUserTypeSelected(lable: String) {
        _uiState.update { it.copy(selected = lable) }
    }

    fun onPlayingChanged(playing: Boolean) {
        _uiState.update { it.copy(isPlaying = playing) }
    }

    fun onProgressChanged(
        progress: Float,
        currentMs: Long,
        durationMs: Long
    ) {

        _uiState.update {
            it.copy(
                progress = progress,
                currentMs = currentMs,
                durationMs = durationMs
            )
        }

    }

    fun onFinish() {
        viewModelScope.launch {
            onboardingPreferences.setOnboardingCompleted()
        }
    }
}