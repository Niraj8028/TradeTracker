package com.wallstreet.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallstreet.domain.model.User
import com.wallstreet.domain.usecase.auth.DeleteAccountUseCase
import com.wallstreet.domain.usecase.auth.GetCurrentUserUseCase
import com.wallstreet.domain.usecase.auth.SignOutUseCase
import com.wallstreet.presentation.profile.screens.DeleteUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.wallstreet.core.result.Result
import com.wallstreet.presentation.profile.screens.DeleteUiState.*

class ProfileViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase

) : ViewModel() {

    val user: User? = getCurrentUserUseCase()
    private val _isLoggedOut = MutableStateFlow(false)
    val isLoggedOut: StateFlow<Boolean> = _isLoggedOut.asStateFlow()
    private val _deleteState = MutableStateFlow<DeleteUiState>(DeleteUiState.Idle)
    val deleteState: StateFlow<DeleteUiState> = _deleteState.asStateFlow()
    fun signOut() = viewModelScope.launch {
        signOutUseCase()
        _isLoggedOut.value = true
    }

    fun deleteAccount() = viewModelScope.launch {
        _deleteState.value = DeleteUiState.Loading
        val result: Result<Boolean> = deleteAccountUseCase()
        when (result) {

            is Result.Success -> {
                _deleteState.value = DeleteUiState.Success
            }

            is Result.Error -> {
                _deleteState.value = Error(result.message)
            }

            Result.Loading -> {
                _deleteState.value = DeleteUiState.Loading

            }
        }
    }
}