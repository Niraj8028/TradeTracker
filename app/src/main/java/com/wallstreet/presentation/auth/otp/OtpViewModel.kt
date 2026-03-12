package com.wallstreet.presentation.auth.otp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallstreet.core.result.Result
import com.wallstreet.domain.usecase.auth.VerifyOtpUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch


data class OtpUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class OtpViewModel(private val verifyOtp: VerifyOtpUseCase) : ViewModel() {
    val _uiState = MutableStateFlow(OtpUiState())

    fun verifyOtp() = viewModelScope.launch {
        _uiState.value = OtpUiState(isSuccess = true)

        when (val r = verifyOtp.invoke()) {
            is Result.Success -> {
                if (r.data == true) {
                    _uiState.value = OtpUiState(isSuccess = true)
                } else {
                    _uiState.value = OtpUiState(
                        error = "Email not verified yet.\nPlease check your inbox and click the link."
                    )
                }

            }

            is Result.Error -> {
                _uiState.value = OtpUiState(error = r.message)
            }

            is Result.Loading -> {
                _uiState.value = OtpUiState(isLoading = true)
            }

        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}