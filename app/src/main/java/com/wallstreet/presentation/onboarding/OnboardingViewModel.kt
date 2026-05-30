package com.wallstreet.presentation.onboarding

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallstreet.domain.analytics.AnalyticsManager
import com.wallstreet.domain.usecase.onboarding.CompleteOnboardingUseCase
import com.wallstreet.core.result.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val analyticsManager: AnalyticsManager,
    private val completeOnboardingUseCase: CompleteOnboardingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private var videoStartLogged = false
    private var videoCompleteLogged = false
    private var isFinishing = false

    init {
        analyticsManager.logEvent("onboarding_start")
    }

    fun onUserTypeSelected(role: String) {
        val isSelecting = !_uiState.value.selectedRoles.contains(role)
        
        // Log event outside of update block for thread safety
        analyticsManager.logEvent("role_selected", mapOf(
            "role_name" to role,
            "is_selected" to isSelecting
        ))

        _uiState.update { state ->
            val newSelected = if (isSelecting) {
                state.selectedRoles + role
            } else {
                state.selectedRoles - role
            }
            state.copy(selectedRoles = newSelected)
        }
    }

    fun onPageSwiped(page: Int) {
        if (page == 1 && !videoStartLogged) {
            analyticsManager.logEvent("tutorial_video_start")
            videoStartLogged = true
        }
    }

    fun onPlayingChanged(playing: Boolean) {
        _uiState.update { it.copy(isPlaying = playing) }
    }

    fun onProgressChanged(
        progress: Float,
        currentMs: Long,
        durationMs: Long
    ) {
        if (progress >= 0.99f && !videoCompleteLogged) {
            analyticsManager.logEvent("tutorial_video_complete")
            videoCompleteLogged = true
        }

        _uiState.update {
            it.copy(
                progress = progress,
                currentMs = currentMs,
                durationMs = durationMs
            )
        }
    }

    fun onFinish() {
        if (isFinishing) return
        isFinishing = true

        viewModelScope.launch {
            val roles = _uiState.value.selectedRoles
            
            // 1. Set User Property (List as comma-separated string)
            analyticsManager.setUserProperty("user_roles", roles.joinToString(","))
            
            // 2. Log Completion Event
            analyticsManager.logEvent("onboarding_complete", mapOf(
                "roles_count" to roles.size
            ))

            // 3. Complete onboarding via UseCase (handles Firestore & Preferences)
            val result = completeOnboardingUseCase(roles.toSet())
            if (result is Result.Error) {
                isFinishing = false
                // Handle error if needed, maybe show a toast or log it
            }
        }
    }
}
