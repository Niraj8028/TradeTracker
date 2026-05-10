package com.wallstreet.presentation.auth.verification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallstreet.core.result.Result
import com.wallstreet.domain.usecase.auth.VerifyOtpUseCase
import com.wallstreet.domain.usecase.auth.ResendVerificationEmailUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch


data class OtpUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val isResending: Boolean = false,
    val resendSuccess: Boolean = false
)

class EmailVerifyViewModel(
    private val verifyOtp: VerifyOtpUseCase,
    private val resendVerificationEmail: ResendVerificationEmailUseCase
) : ViewModel() {
    val _uiState = MutableStateFlow(OtpUiState())

    init {
        startPolling()
    }

    private fun startPolling() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true)

        while (true) {
            delay(3000L) // check every 3 seconds

            when (val r = verifyOtp.invoke()) {
                is Result.Success -> {
                    if (r.data == true) {
                        _uiState.value = _uiState.value.copy(isSuccess = true, isLoading = false)
                        break // stop polling, navigate away
                    }
                    // not verified yet, keep polling silently
                }

                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(error = r.message, isLoading = false)
                    break // stop on error
                }

                else -> {}
            }
        }
    }

    fun resendEmail() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isResending = true)
        when (val r = resendVerificationEmail.invoke()) {
            is Result.Success -> {
                _uiState.value = _uiState.value.copy(isResending = false, resendSuccess = true)
            }
            is Result.Error -> {
                _uiState.value = _uiState.value.copy(isResending = false, error = r.message)
            }
            else -> {}
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null, resendSuccess = false)
    }
}