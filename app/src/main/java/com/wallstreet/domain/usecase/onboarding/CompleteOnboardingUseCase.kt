package com.wallstreet.domain.usecase.onboarding

import com.wallstreet.core.preferences.OnboardingPreferences
import com.wallstreet.domain.repository.AuthRepository
import com.wallstreet.domain.repository.UserRepository
import com.wallstreet.core.result.Result
import timber.log.Timber

class CompleteOnboardingUseCase(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val onboardingPreferences: OnboardingPreferences
) {
    suspend operator fun invoke(roles: Set<String>): Result<Unit> {
        return try {
            Timber.d("CompleteOnboardingUseCase: Starting completion flow")
            // 1. Update Firestore if user is authenticated
            authRepository.getCurrentUser()?.id?.let { userId ->
                Timber.d("CompleteOnboardingUseCase: Saving roles and status for userId=$userId")
                userRepository.saveUserRoles(userId, roles.toList())
                userRepository.updateOnboardingStatus(userId, true)
            }

            // 3. Mark onboarding as completed locally
            onboardingPreferences.setOnboardingCompleted()
            Timber.d("CompleteOnboardingUseCase: Local preferences updated")
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "CompleteOnboardingUseCase: Error during completion")
            Result.Error(e.message ?: "Failed to complete onboarding", e)
        }
    }
}
