package com.wallstreet.domain.repository

import com.wallstreet.core.result.Result

/** Account-level preferences that live on the user's Firestore doc (portable across devices). */
data class AccountPrefs(
    val onboardingCompleted: Boolean,
    val currencyCode: String?,
    val roles: List<String> = emptyList(),
)

interface UserRepository {
    suspend fun saveUserRoles(userId: String, roles: List<String>): Result<Unit>

    /** Marks onboarding done on the user's account doc (source of truth across devices). */
    suspend fun setOnboardingCompleted(userId: String): Result<Unit>

    /** Persists the user's preferred currency on their account doc. */
    suspend fun setCurrencyCode(userId: String, code: String): Result<Unit>

    /**
     * Reads the account prefs in a single doc fetch. `onboardingCompleted` is the explicit
     * flag OR previously-saved roles (covers accounts onboarded before the flag existed).
     */
    suspend fun getAccountPrefs(userId: String): Result<AccountPrefs>
}
