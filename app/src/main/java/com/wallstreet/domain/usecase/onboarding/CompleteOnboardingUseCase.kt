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
                ?: return Result.Error("No signed-in user to complete onboarding for")

            // Every account-doc write must land before onboarding is treated as done — the
            // repo impls return Result.Error instead of throwing, so check each explicitly.
            // Bail on the first failure so the local cache is never set ahead of Firestore.
            userRepository.saveUserRoles(userId, roles.toList()).let {
                if (it is Result.Error) return it
            }
            userRepository.setCurrencyCode(userId, currencyCode).let {
                if (it is Result.Error) return it
            }
            // Source of truth so onboarding never re-appears on another device / reinstall.
            userRepository.setOnboardingCompleted(userId).let {
                if (it is Result.Error) return it
            }
            // Local per-user cache to skip the Firestore read on later logins here.
            onboardingPreferences.setOnboardingCompleted(userId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to complete onboarding", e)
        }
    }
}
