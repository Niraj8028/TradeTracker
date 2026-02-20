package com.wallstreet.domain.usecase.auth



import com.wallstreet.core.result.Result
import com.wallstreet.domain.model.User
import com.wallstreet.domain.repository.AuthRepository

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
class SignUpUseCase(private val repo: AuthRepository) {
    suspend operator fun invoke(
        fullName: String, email: String,
        password: String, confirmPassword: String
    ): Result<User> {
        if (fullName.isBlank()) return Result.Error("Name cannot be empty")
        if (email.isBlank()) return Result.Error("Email cannot be empty")
        if (password.length < 6) return Result.Error("Password must be at least 6 characters")
        if (password != confirmPassword) return Result.Error("Passwords do not match")
        return repo.signUp(fullName, email, password)
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