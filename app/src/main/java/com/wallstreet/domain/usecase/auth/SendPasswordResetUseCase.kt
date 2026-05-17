package com.wallstreet.domain.usecase.auth

import com.wallstreet.core.result.Result
import com.wallstreet.domain.repository.AuthRepository

class SendPasswordResetUseCase(private val repo: AuthRepository) {
    suspend operator fun invoke(email: String): Result<Unit> {
        return repo.sendPasswordResetEmail(email)
    }
}