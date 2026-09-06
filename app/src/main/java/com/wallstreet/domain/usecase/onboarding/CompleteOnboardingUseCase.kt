package com.wallstreet.domain.usecase.onboarding

import com.wallstreet.core.preferences.OnboardingPreferences
import com.wallstreet.domain.repository.AuthRepository
import com.wallstreet.domain.repository.UserRepository
import com.wallstreet.core.result.Result

class CompleteOnboardingUseCase(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val onboardingPreferences: OnboardingPreferences
) {
    suspend operator fun invoke(roles: Set<String>, currencyCode: String): Result<Unit> {
        return try {
            val userId = authRepository.getCurrentUser()?.id
            if (userId != null) {
                userRepository.saveUserRoles(userId, roles.toList())
                userRepository.setCurrencyCode(userId, currencyCode)
                // Source of truth so onboarding never re-appears on another device / reinstall.
                userRepository.setOnboardingCompleted(userId)
                // Local per-user cache to skip the Firestore read on later logins here.
                onboardingPreferences.setOnboardingCompleted(userId)
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to complete onboarding", e)
        }
    }
}
