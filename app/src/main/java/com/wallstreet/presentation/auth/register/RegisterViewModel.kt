package com.wallstreet.presentation.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallstreet.core.result.Result
import com.wallstreet.domain.usecase.auth.SignUpUseCase
import com.wallstreet.domain.usecase.auth.SignInWithGoogleUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RegisterUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val navigateToOtp: Boolean = false
)

class RegisterViewModel(
    private val signUp: SignUpUseCase,
    private val signInWithGoogle: SignInWithGoogleUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun signUp(fullName: String, email: String, password: String, confirmPassword: String) =
        viewModelScope.launch {
            if (fullName.isBlank()) {
                _uiState.value = RegisterUiState(error = "ERROR_NAME_EMPTY")
                return@launch
            }
            if (email.isBlank()) {
                _uiState.value = RegisterUiState(error = "ERROR_EMAIL_EMPTY")
                return@launch
            }
            if (password.length < 6) {
                _uiState.value = RegisterUiState(error = "ERROR_PASSWORD_TOO_SHORT")
                return@launch
            }
            if (password != confirmPassword) {
                _uiState.value = RegisterUiState(error = "ERROR_PASSWORDS_DO_NOT_MATCH")
                return@launch
            }

            _uiState.value = RegisterUiState(isLoading = true)

            _uiState.value = when (val r = signUp.invoke(fullName, email, password, confirmPassword)) {
                is Result.Success -> RegisterUiState(navigateToOtp = true)
                is Result.Error -> RegisterUiState(error = r.message)
                is Result.Loading -> RegisterUiState(isLoading = true)
            }
        }

    fun signUpWithGoogle(idToken: String) = viewModelScope.launch {
        _uiState.value = RegisterUiState(isLoading = true)
        _uiState.value = when (val r = signInWithGoogle.invoke(idToken)) {
            is Result.Success -> RegisterUiState(isSuccess = true)
            is Result.Error   -> RegisterUiState(error = r.message)
            is Result.Loading -> RegisterUiState(isLoading = true)
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }

    /** Consume the one-shot navigate-to-OTP event so it cannot re-fire when coming back to this screen. */
    fun resetNavigation() { _uiState.value = _uiState.value.copy(navigateToOtp = false) }
}