package com.wallstreet.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.wallstreet.core.constants.AppConstants
import com.wallstreet.core.result.Result
import com.wallstreet.domain.repository.AccountPrefs
import com.wallstreet.domain.repository.UserRepository
import kotlinx.coroutines.tasks.await

class UserRepositoryImpl(
    private val firestore: FirebaseFirestore
) : UserRepository {

    private fun userDoc(userId: String) =
        firestore.collection(AppConstants.COLLECTION_USERS).document(userId)

    override suspend fun saveUserRoles(userId: String, roles: List<String>): Result<Unit> {
        return try {
            userDoc(userId).set(mapOf("roles" to roles), SetOptions.merge()).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to save user roles", e)
        }
    }

    override suspend fun setOnboardingCompleted(userId: String): Result<Unit> {
        return try {
            userDoc(userId).set(mapOf("onboardingCompleted" to true), SetOptions.merge()).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update onboarding status", e)
        }
    }

    override suspend fun setCurrencyCode(userId: String, code: String): Result<Unit> {
        return try {
            userDoc(userId).set(mapOf("currencyCode" to code), SetOptions.merge()).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to save currency", e)
        }
    }

    override suspend fun getAccountPrefs(userId: String): Result<AccountPrefs> {
        return try {
            val snap = userDoc(userId).get().await()
            val explicit = snap.getBoolean("onboardingCompleted") == true
            val roles = snap.get("roles") as? List<*>
            val currency = snap.getString("currencyCode")
            Result.Success(
                AccountPrefs(
                    onboardingCompleted = explicit || !roles.isNullOrEmpty(),
                    currencyCode = currency,
                    roles = roles.orEmpty().filterIsInstance<String>(),
                )
            )
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to read account prefs", e)
        }
    }
}
