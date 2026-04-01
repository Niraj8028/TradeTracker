package com.wallstreet.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.wallstreet.core.result.Result
import com.wallstreet.data.mapper.toDto
import com.wallstreet.data.remote.FirebaseService
import com.wallstreet.domain.model.UserStrategy
import com.wallstreet.domain.repository.UserStrategyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class UserStrategyImpl(private val auth: FirebaseAuth, private val firestore: FirebaseFirestore) :
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

    override suspend fun getStrategy(): Flow<List<UserStrategy>> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteStrategy(): Result<String> {
        TODO("Not yet implemented")
    }

    override suspend fun updateStrategy(): Result<String> {
        TODO("Not yet implemented")
    }


}