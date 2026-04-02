package com.wallstreet.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.wallstreet.core.result.Result
import com.wallstreet.data.mapper.toDomain
import com.wallstreet.data.mapper.toDto
import com.wallstreet.data.remote.FirebaseService
import com.wallstreet.domain.model.UserStrategy
import com.wallstreet.domain.repository.AuthRepository
import com.wallstreet.domain.repository.UserStrategyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose

class UserStrategyImpl(
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore
) :
    UserStrategyRepository {

    private val strategyCollection =
        FirebaseService.firestore.collection(FirebaseService.Collections.STRATEGIES)

    override suspend fun addStrategy(userStrategy: UserStrategy): Result<String> {
        return try {

            val userStrategyDto = userStrategy.toDto()
            val docRef = strategyCollection.add(userStrategyDto).await()
            Result.Success(docRef.id);

        } catch (e: Exception) {
            Result.Error(e.toString());

        }
    }

    override suspend fun getStrategy(): Flow<List<UserStrategy>> = callbackFlow {
        val userId = authRepository.getCurrentUser()!!.id

        val listener = strategyCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val strategies = snapshot?.documents?.mapNotNull { doc ->
                    val dto = doc.toObject(UserStrategyDto::class.java)
                    dto?.copy(id = doc.id)?.toDomain()
                } ?: emptyList()

                trySend(strategies)
            }

        awaitClose {
            listener.remove()
        }
    }

    override suspend fun deleteStrategy(userStrategy: UserStrategy): Result<String> {

        return try {
            val id = userStrategy.id
            if (id.isBlank()) return Result.Error("Missing strategy Id")
            strategyCollection.document(id).delete().await()
            Result.Success("Strategy deleted");

        } catch (e: Exception) {
            Result.Error(e.toString());

        }
    }

    override suspend fun updateStrategy(userStrategy: UserStrategy): Result<String> {
        return try {
            val id = userStrategy.id
            if (id.isBlank()) return Result.Error("Missing strategy Id")
            strategyCollection.document(id).set(userStrategy).await()
            Result.Success("Strategy updated successfully")

        } catch (e: Exception) {
            Result.Error(e.toString());

        }
    }


}