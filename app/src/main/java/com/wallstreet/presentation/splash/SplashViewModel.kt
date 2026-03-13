package com.wallstreet.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.wallstreet.core.splash.StartDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SplashDestination {
    data object None : SplashDestination
    data object Home : SplashDestination
    data object Otp : SplashDestination
    data object Onboarding : SplashDestination
}

class SplashViewModel(
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination>(SplashDestination.None)
    val destination: StateFlow<SplashDestination> = _destination.asStateFlow()

    fun checkAuthState() = viewModelScope.launch {
        val user = auth.currentUser

        _destination.value = when {
            user == null -> SplashDestination.Onboarding

            user.isEmailVerified -> SplashDestination.Home

            else -> SplashDestination.Otp
        }
    }
}