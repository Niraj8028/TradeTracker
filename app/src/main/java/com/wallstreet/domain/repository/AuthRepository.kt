package com.wallstreet.domain.repository

import com.wallstreet.core.result.AuthState
import com.wallstreet.core.result.Result
import com.wallstreet.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signInWithEmail(email: String, password: String): Result<User>
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun signUp(fullName: String, email: String, password: String): Result<User>
    suspend fun signOut()
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun verifyEmail(): Result<Boolean>
    fun getCurrentUser(): User?
    fun observeAuthState(): Flow<AuthState>
}