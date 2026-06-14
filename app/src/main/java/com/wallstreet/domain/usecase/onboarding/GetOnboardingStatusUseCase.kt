package com.wallstreet.domain.usecase.onboarding

import com.wallstreet.core.preferences.OnboardingPreferences
import com.wallstreet.domain.repository.UserRepository
import com.wallstreet.core.result.Result
import timber.log.Timber

class GetOnboardingStatusUseCase(
    private val userRepository: UserRepository,
    private val onboardingPreferences: OnboardingPreferences
) {
    suspend operator fun invoke(userId: String): Boolean {
        Timber.d("GetOnboardingStatusUseCase: Checking for userId=$userId")
        return when (val result = userRepository.getOnboardingStatus(userId)) {
            is Result.Success -> {
                Timber.d("GetOnboardingStatusUseCase: result=${result.data}")
                if (result.data) {
                    onboardingPreferences.setOnboardingCompleted()
                }
                result.data
            }
            is Result.Error -> {
                Timber.e("GetOnboardingStatusUseCase: Error=${result.message}")
                false
            }
            else -> false // Fallback
        }
    }
}
