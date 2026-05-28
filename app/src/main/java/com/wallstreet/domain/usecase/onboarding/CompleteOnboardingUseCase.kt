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
    suspend operator fun invoke(roles: Set<String>): Result<Unit> {
        return try {
            // 1. Update Firestore if user is authenticated
            authRepository.getCurrentUser()?.id?.let { userId ->
                userRepository.saveUserRoles(userId, roles.toList())
            }

            // 2. Mark onboarding as completed
            onboardingPreferences.setOnboardingCompleted()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to complete onboarding", e)
        }
    }
}