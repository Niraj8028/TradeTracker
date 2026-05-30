package com.wallstreet.domain.repository

import com.wallstreet.core.result.Result

interface UserRepository {
    suspend fun saveUserRoles(userId: String, roles: List<String>): Result<Unit>
}
