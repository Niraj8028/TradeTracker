package com.wallstreet.core.result

import com.wallstreet.domain.model.User

// TODO use this Authstate in AuthRepository
sealed class AuthState {
    object Loading: AuthState()
    object UnAuthenticated: AuthState()
    data class Authenticated(val user: User): AuthState()
}