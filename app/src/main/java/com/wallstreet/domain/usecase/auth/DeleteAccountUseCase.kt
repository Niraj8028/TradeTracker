package com.wallstreet.domain.usecase.auth

import com.wallstreet.domain.model.User
import com.wallstreet.domain.repository.AuthRepository
import com.wallstreet.core.result.Result

class DeleteAccountUseCase(private val repo: AuthRepository) {
    suspend operator fun invoke(): Result<Boolean> {
        return repo.deleteAccount()
    }
}