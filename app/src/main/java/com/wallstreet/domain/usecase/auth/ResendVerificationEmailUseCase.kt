package com.wallstreet.domain.usecase.auth

import com.wallstreet.core.result.Result
import com.wallstreet.domain.repository.AuthRepository

class ResendVerificationEmailUseCase(
    private val repository: AuthRepository
) {
    suspend fun invoke(): Result<Unit> {
        return repository.resendVerificationEmail()
    }
}