package com.wallstreet.presentation.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallstreet.core.result.Result
import com.wallstreet.domain.usecase.auth.SendPasswordResetEmailUseCase
import com.wallstreet.domain.usecase.auth.SignInUseCase
import com.wallstreet.domain.usecase.auth.SignInWithGoogleUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class LoginViewModel(
    private val signIn: SignInUseCase,
    private val signInWithGoogle: SignInWithGoogleUseCase,
    private val sendPasswordResetEmail: SendPasswordResetEmailUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun signIn(email: String, password: String) = viewModelScope.launch {
        if (email.isBlank()) {
            _uiState.value = LoginUiState(error = "ERROR_EMAIL_EMPTY")
            return@launch
        }
        if (password.length < 6) {
            _uiState.value = LoginUiState(error = "ERROR_PASSWORD_TOO_SHORT")
            return@launch
        }
        _uiState.value = LoginUiState(isLoading = true)
        when (val r = signIn.invoke(email, password)) {
            is Result.Success -> {
                _uiState.value = LoginUiState(isSuccess = true)
            }

            is Result.Error -> {
                if (r.message == "EMAIL_NOT_VERIFIED") {
                    _uiState.value = LoginUiState(navigateToOtp = true)
                } else {
                    _uiState.value = LoginUiState(error = r.message)
                }
            }

            Result.Loading -> _uiState.value = LoginUiState(isLoading = true)

        }
    }

    fun signInWithGoogle(idToken: String) = viewModelScope.launch {
        _uiState.value = LoginUiState(isLoading = true)
        _uiState.value = when (val r = signInWithGoogle.invoke(idToken)) {
            is Result.Success -> {
                LoginUiState(isSuccess = true)
            }

            is Result.Error -> LoginUiState(error = r.message)
            is Result.Loading -> LoginUiState(isLoading = true)
        }
    }

    fun forgotPassword(email: String) = viewModelScope.launch {
        if (email.isBlank()) {
            _uiState.value = LoginUiState(error = "Please enter your email address")
            return@launch
        }
        _uiState.value = LoginUiState(isLoading = true)
        when (val r = sendPasswordResetEmail.invoke(email)) {
            is Result.Success -> _uiState.value = LoginUiState(resetEmailSent = true)
            is Result.Error -> _uiState.value = LoginUiState(error = r.message)
            else -> {}
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}