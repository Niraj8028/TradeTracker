package com.wallstreet.presentation.auth.verification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallstreet.core.result.Result
import com.wallstreet.domain.usecase.auth.VerifyOtpUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch


data class OtpUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class EmailVerifyViewModel(private val verifyOtp: VerifyOtpUseCase) : ViewModel() {
    val _uiState = MutableStateFlow(OtpUiState())

    init {
        startPolling()
    }

    private fun startPolling() = viewModelScope.launch {
        _uiState.value = OtpUiState(isLoading = true)

        while (true) {
            delay(3000L) // check every 3 seconds

            when (val r = verifyOtp.invoke()) {
                is Result.Success -> {
                    if (r.data == true) {
                        _uiState.value = OtpUiState(isSuccess = true)
                        break // stop polling, navigate away
                    }
                    // not verified yet, keep polling silently
                }

                is Result.Error -> {
                    _uiState.value = OtpUiState(error = r.message)
                    break // stop on error
                }

                else -> {}
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}