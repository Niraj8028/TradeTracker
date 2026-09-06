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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber


data class OtpUiState(
    val isLoading: Boolean = false,
    val isChecking: Boolean = false,
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

    /** Reference to the active polling coroutine so it can be paused/cancelled on demand. */
    private var pollingJob: Job? = null

    /**
     * Start (or resume) the background verification poll. Idempotent — safe to call from a
     * lifecycle observer on every ON_START.
     */
    fun startPolling() {
        if (pollingJob?.isActive == true || _uiState.value.isSuccess) return
        pollingJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            while (isActive) {
                delay(3000L) // check every 3 seconds
                when (val r = verifyOtp.invoke()) {
                    is Result.Success -> {
                        if (r.data == true) {
                            _uiState.value = _uiState.value.copy(isSuccess = true, isLoading = false)
                            break
                        }
                        // not verified yet — keep polling silently
                    }

                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(error = r.message, isLoading = false)
                        break
                    }

                    else -> {}
                }
            }
        }
    }

    /** Pause the poll while the screen is not visible; [startPolling] resumes it. */
    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    /** One-shot check triggered by the "I've verified" button — no waiting for the 3s cycle. */
    fun checkNow() = viewModelScope.launch {
        if (_uiState.value.isChecking || _uiState.value.isSuccess) return@launch
        _uiState.value = _uiState.value.copy(isChecking = true)
        when (val r = verifyOtp.invoke()) {
            is Result.Success -> {
                if (r.data == true) {
                    stopPolling()
                    _uiState.value = _uiState.value.copy(
                        isSuccess = true, isChecking = false, isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isChecking = false, error = "NOT_VERIFIED_YET"
                    )
                }
            }

            is Result.Error ->
                _uiState.value = _uiState.value.copy(isChecking = false, error = r.message)

            else -> _uiState.value = _uiState.value.copy(isChecking = false)
        }
    }

    /**
     * Called when the user deliberately taps "Wrong email? Go back".
     * Stops polling, deletes the unverified Firebase account, and signs out so the
     * auth-state listener cannot push the app back to this screen.
     */
    fun abandon() {
        stopPolling()
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
