package com.wallstreet.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.wallstreet.core.result.Result
import com.wallstreet.data.mapper.toDomain
import com.wallstreet.data.mapper.toDto
import com.wallstreet.data.model.strategyDto
import com.wallstreet.data.remote.FirebaseService
import com.wallstreet.domain.model.Strategy
import com.wallstreet.domain.repository.AuthRepository
import com.wallstreet.domain.repository.StrategyRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class StrategyRepositoryImpl(
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore
) : StrategyRepository {

    private val strategyCollection =
        firestore.collection(FirebaseService.Collections.STRATEGIES)

    override suspend fun addStrategy(strategy: Strategy): Result<String> {
        return try {
            val userId = authRepository.getCurrentUser()?.id
                ?: return Result.Error("User not logged in")

            val dto = strategy.copy(
                userId = userId,
                createAt = System.currentTimeMillis()
            ).toDto()

            val docRef = strategyCollection.add(dto).await()
            Result.Success(docRef.id)

        } catch (e: Exception) {
            Result.Error(e.toString())
        }
    }

    override suspend fun getStrategy(): Flow<List<Strategy>> = callbackFlow {
        val userId = authRepository.getCurrentUser()!!.id

        val listener = strategyCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val strategies = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(strategyDto::class.java)?.toDomain()
                } ?: emptyList()

                trySend(strategies)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun deleteStrategy(strategy: Strategy): Result<String> {
        return try {
            val id = strategy.id
            if (id.isBlank()) return Result.Error("Missing strategy Id")
            strategyCollection.document(id).delete().await()
            Result.Success("Strategy deleted")

        } catch (e: Exception) {
            Result.Error(e.toString())
        }
    }

    override suspend fun updateStrategy(strategy: Strategy): Result<String> {
        return try {
            val id = strategy.id
            if (id.isBlank()) return Result.Error("Missing strategy Id")
            strategyCollection.document(id).set(strategy.toDto()).await()
            Result.Success("Strategy updated successfully")

        } catch (e: Exception) {
            Result.Error(e.toString())
        }
    }
}