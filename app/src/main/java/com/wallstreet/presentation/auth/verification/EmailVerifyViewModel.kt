package com.wallstreet.presentation.auth.verification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallstreet.core.result.Result
import com.wallstreet.domain.usecase.auth.DeleteAccountUseCase
import com.wallstreet.domain.usecase.auth.ResendVerificationEmailUseCase
import com.wallstreet.domain.usecase.auth.SignOutUseCase
import com.wallstreet.domain.usecase.auth.VerifyOtpUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber


data class OtpUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val isResending: Boolean = false,
    val resendSuccess: Boolean = false
)

class EmailVerifyViewModel(
    private val verifyOtp: VerifyOtpUseCase,
    private val resendVerificationEmail: ResendVerificationEmailUseCase,
    private val deleteAccount: DeleteAccountUseCase,
    private val signOut: SignOutUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OtpUiState())
    val uiState: StateFlow<OtpUiState> = _uiState.asStateFlow()

    /** Holds a reference to the active polling coroutine so we can cancel it on demand. */
    private var pollingJob: Job? = null

    init {
        startPolling()
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
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
    }

    /**
     * Called when the user deliberately taps "Wrong email? Go back".
     * Stops the polling loop, deletes the unverified Firebase account, and signs out
     * so the auth-state listener cannot push the app back to this screen.
     */
    fun abandon() {
        pollingJob?.cancel()
        pollingJob = null

        viewModelScope.launch {
            try {
                deleteAccount()
            } catch (e: Exception) {
                Timber.w(e, "EmailVerifyViewModel: could not delete unverified user")
            } finally {
                signOut()
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