package com.wallstreet.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallstreet.core.result.Result
import com.wallstreet.domain.model.User
import com.wallstreet.domain.repository.TradeRepository
import com.wallstreet.domain.usecase.auth.DeleteAccountUseCase
import com.wallstreet.domain.usecase.auth.GetCurrentUserUseCase
import com.wallstreet.domain.usecase.auth.SignOutUseCase
import com.wallstreet.presentation.profile.screens.DeleteUiState
import com.wallstreet.presentation.profile.screens.DeleteUiState.Error
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val tradeRepository: TradeRepository
) : ViewModel() {

    val user: User? = getCurrentUserUseCase()

    private val _isLoggedOut = MutableStateFlow(false)
    val isLoggedOut: StateFlow<Boolean> = _isLoggedOut.asStateFlow()

    private val _deleteState = MutableStateFlow<DeleteUiState>(DeleteUiState.Idle)
    val deleteState: StateFlow<DeleteUiState> = _deleteState.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    // true = show "unsynced trades" warning dialog before logout
    private val _showUnsyncedWarning = MutableStateFlow(false)
    val showUnsyncedWarning: StateFlow<Boolean> = _showUnsyncedWarning.asStateFlow()

    fun onSignOutClicked() = viewModelScope.launch {
        val userId = user?.id ?: return@launch

        if (!tradeRepository.hasPendingTrades(userId)) {
            performSignOut(userId)
            return@launch
        }

        // Has pending trades — attempt sync first
        _isSyncing.value = true
        val syncSucceeded = tradeRepository.syncPendingTrades(userId)
        _isSyncing.value = false

        if (syncSucceeded) {
            performSignOut(userId)
        } else {
            // Offline or partial failure — warn the user
            _showUnsyncedWarning.value = true
        }
    }

    fun confirmSignOutAnyway() = viewModelScope.launch {
        _showUnsyncedWarning.value = false
        user?.id?.let { performSignOut(it) }
    }

    fun dismissUnsyncedWarning() {
        _showUnsyncedWarning.value = false
    }

    private suspend fun performSignOut(userId: String) {
        tradeRepository.clearLocalData(userId)
        signOutUseCase()
        _isLoggedOut.value = true
    }

    fun deleteAccount() = viewModelScope.launch {
        _deleteState.value = DeleteUiState.Loading
        when (val result: Result<Boolean> = deleteAccountUseCase()) {
            is Result.Success -> _deleteState.value = DeleteUiState.Success
            is Result.Error -> _deleteState.value = Error(result.message)
            Result.Loading -> _deleteState.value = DeleteUiState.Loading
        }
    }
}
