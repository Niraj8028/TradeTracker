package com.wallstreet.domain.repository

import com.wallstreet.core.result.Result
import com.wallstreet.domain.model.User

interface AuthRepository {
    suspend fun signInWithEmail(email: String, password: String): Result<User>
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun signUp(fullName: String, email: String, password: String): Result<User>
    suspend fun signOut()

     suspend fun verifyOtp(): Result<Boolean>
    fun getCurrentUser(): User?
}