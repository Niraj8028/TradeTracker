package com.wallstreet.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallstreet.domain.model.User
import com.wallstreet.domain.usecase.auth.GetCurrentUserUseCase
import com.wallstreet.domain.usecase.auth.SignOutUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val signOutUseCase: SignOutUseCase

) : ViewModel() {

    val user: User? = getCurrentUserUseCase()
    private val _isLoggedOut = MutableStateFlow(false)
    val isLoggedOut: StateFlow<Boolean> = _isLoggedOut.asStateFlow()

    fun signOut() = viewModelScope.launch {
        signOutUseCase()
        _isLoggedOut.value = true
    }
}