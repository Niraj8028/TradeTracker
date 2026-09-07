package com.wallstreet.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallstreet.core.preferences.CurrencyPreferences
import com.wallstreet.core.result.Result
import com.wallstreet.domain.analytics.AnalyticsManager
import com.wallstreet.domain.usecase.onboarding.CompleteOnboardingUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val analyticsManager: AnalyticsManager,
    private val completeOnboardingUseCase: CompleteOnboardingUseCase,
    private val currencyPreferences: CurrencyPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        analyticsManager.logEvent("onboarding_start")
        analyticsManager.logEvent("onboarding_step_view", mapOf("step" to 0))
    }

    fun onRoleToggled(role: String) {
        val isSelecting = !_uiState.value.selectedRoles.contains(role)
        analyticsManager.logEvent(
            "role_selected",
            mapOf("role_name" to role, "is_selected" to isSelecting)
        )
        _uiState.update { state ->
            val newSelected = if (isSelecting) state.selectedRoles + role
            else state.selectedRoles - role
            state.copy(selectedRoles = newSelected)
        }
    }

    fun onCurrencySelected(code: String) {
        _uiState.update { it.copy(selectedCurrency = code) }
        analyticsManager.logEvent("currency_selected", mapOf("code" to code))
    }

    fun goToStep(index: Int) {
        if (index == _uiState.value.stepIndex) return
        _uiState.update { it.copy(stepIndex = index) }
        analyticsManager.logEvent("onboarding_step_view", mapOf("step" to index))
    }

    fun nextStep(lastIndex: Int) {
        goToStep((_uiState.value.stepIndex + 1).coerceAtMost(lastIndex))
    }

    fun prevStep() {
        goToStep((_uiState.value.stepIndex - 1).coerceAtLeast(0))
    }

    fun onNotificationsResult(granted: Boolean) {
        analyticsManager.logEvent("notifications_prompt_result", mapOf("granted" to granted))
    }

    fun finish() {
        if (_uiState.value.isFinishing) return
        _uiState.update { it.copy(isFinishing = true, error = null) }

        viewModelScope.launch {
            val state = _uiState.value

            analyticsManager.setUserProperty("user_roles", state.selectedRoles.joinToString(","))
            analyticsManager.logEvent(
                "onboarding_complete",
                mapOf("roles_count" to state.selectedRoles.size)
            )

            when (val result =
                completeOnboardingUseCase(state.selectedRoles.toSet(), state.selectedCurrency)) {
                is Result.Error -> _uiState.update {
                    it.copy(
                        isFinishing = false,
                        error = result.message.ifBlank { "Couldn't finish setup. Please try again." },
                    )
                }

                else -> {
                    // Only mirror the choice into the local cache once the account write landed.
                    currencyPreferences.setCurrency(state.selectedCurrency)
                    _uiState.update { it.copy(finished = true) }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
