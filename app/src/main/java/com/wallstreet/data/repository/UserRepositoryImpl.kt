package com.wallstreet.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.wallstreet.core.constants.AppConstants
import com.wallstreet.core.result.Result
import com.wallstreet.domain.repository.UserRepository
import kotlinx.coroutines.tasks.await

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
}