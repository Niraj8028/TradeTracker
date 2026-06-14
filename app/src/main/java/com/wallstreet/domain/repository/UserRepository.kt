package com.wallstreet.domain.repository

import com.wallstreet.core.result.Result

interface UserRepository {
    suspend fun saveUserRoles(userId: String, roles: List<String>): Result<Unit>
    suspend fun getOnboardingStatus(userId: String): Result<Boolean>
    suspend fun updateOnboardingStatus(userId: String, completed: Boolean): Result<Unit>
}
