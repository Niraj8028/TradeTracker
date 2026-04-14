package com.wallstreet.domain.usecase.auth


import com.wallstreet.core.result.Result
import com.wallstreet.domain.model.Strategy
import com.wallstreet.domain.model.User
import com.wallstreet.domain.repository.AuthRepository
import com.wallstreet.domain.repository.StrategyRepository

class SignInUseCase(private val repo: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        if (email.isBlank()) return Result.Error("Email cannot be empty")
        if (password.length < 6) return Result.Error("Password must be at least 6 characters")
        return repo.signInWithEmail(email, password)
    }
}

// domain/usecase/auth/SignInWithGoogleUseCase.kt
class SignInWithGoogleUseCase(private val repo: AuthRepository) {
    suspend operator fun invoke(idToken: String): Result<User> =
        repo.signInWithGoogle(idToken)
}

// domain/usecase/auth/SignUpUseCase.kt
class SignUpUseCase(
    private val repo: AuthRepository,
    private val strategyRepository: StrategyRepository
) {
    private val defaultStrategies = listOf(
        Strategy(name = "Breakout", description = ""),
        Strategy(name = "9 EMA Strategy", description = ""),
        Strategy(name = "Double Top", description = ""),
        Strategy(name = "Trend Following", description = "")
    )

    suspend operator fun invoke(
        fullName: String, email: String,
        password: String, confirmPassword: String
    ): Result<User> {
        if (fullName.isBlank()) return Result.Error("Name cannot be empty")
        if (email.isBlank()) return Result.Error("Email cannot be empty")
        if (password.length < 6) return Result.Error("Password must be at least 6 characters")
        if (password != confirmPassword) return Result.Error("Passwords do not match")
        val result = repo.signUp(fullName, email, password)
        if (result is Result.Error) return Result.Error("Error signing up")
        runCatching {
            defaultStrategies.forEach { strategyRepository.addStrategy(it) }
        }

        return result
    }
}

class VerifyOtpUseCase(
    private val repository: AuthRepository
) {
    suspend fun invoke(): Result<Boolean> {
        return repository.verifyEmail()
    }
}

class SendPasswordResetEmailUseCase(private val repo: AuthRepository) {
    suspend operator fun invoke(email: String): Result<Unit> {
        if (email.isBlank()) return Result.Error("Please enter your email address")
        return repo.sendPasswordResetEmail(email)
    }
}

// domain/usecase/auth/SignOutUseCase.kt
class SignOutUseCase(private val repo: AuthRepository) {
    suspend operator fun invoke() = repo.signOut()
}

// domain/usecase/auth/GetCurrentUserUseCase.kt
class GetCurrentUserUseCase(private val repo: AuthRepository) {
    operator fun invoke() = repo.getCurrentUser()
}


