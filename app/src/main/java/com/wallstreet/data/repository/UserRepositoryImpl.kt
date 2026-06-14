package com.wallstreet.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.wallstreet.core.constants.AppConstants
import com.wallstreet.core.result.Result
import com.wallstreet.data.model.UserDto
import com.wallstreet.domain.repository.UserRepository
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class UserRepositoryImpl(
    private val firestore: FirebaseFirestore
) : UserRepository {
    override suspend fun saveUserRoles(userId: String, roles: List<String>): Result<Unit> {
        return try {
            firestore.collection(AppConstants.COLLECTION_USERS)
                .document(userId)
                .set(mapOf("roles" to roles), SetOptions.merge())
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to save user roles", e)
        }
    }

    override suspend fun getOnboardingStatus(userId: String): Result<Boolean> {
        return try {
            Timber.d("getOnboardingStatus: Fetching for userId=$userId")
            val document = firestore.collection(AppConstants.COLLECTION_USERS)
                .document(userId)
                .get()
                .await()
            
            val completed = document.getBoolean("onboardingCompleted") ?: false
            Timber.d("getOnboardingStatus: result=$completed for userId=$userId")
            Result.Success(completed)
        } catch (e: Exception) {
            Timber.e(e, "getOnboardingStatus: Failed for userId=$userId")
            Result.Error(e.message ?: "Failed to fetch onboarding status", e)
        }
    }

    override suspend fun updateOnboardingStatus(userId: String, completed: Boolean): Result<Unit> {
        return try {
            Timber.d("updateOnboardingStatus: Setting to $completed for userId=$userId")
            firestore.collection(AppConstants.COLLECTION_USERS)
                .document(userId)
                .set(mapOf("onboardingCompleted" to completed), SetOptions.merge())
                .await()
            Timber.d("updateOnboardingStatus: Successfully updated Firestore for userId=$userId")
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "updateOnboardingStatus: Failed for userId=$userId")
            Result.Error(e.message ?: "Failed to update onboarding status", e)
        }
    }
}
