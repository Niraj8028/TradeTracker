package com.wallstreet.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.wallstreet.core.result.Result
import com.wallstreet.data.mapper.toDomain
import com.wallstreet.data.mapper.toDto
import com.wallstreet.data.model.TradeDto
import com.wallstreet.data.remote.FirebaseService
import com.wallstreet.domain.model.Trade
import com.wallstreet.domain.repository.TradeRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.lang.Error

class TradeRepositoryImpl(
    private val firestore: FirebaseFirestore
): TradeRepository {

    private val tradesCollection = firestore.collection(FirebaseService.Collections.TRADES)

    override fun getAllTrades(userId: String): Flow<List<Trade>> = flow{
         try {
            val snapshot = tradesCollection
                .whereEqualTo("userId", userId)
                .orderBy("createdAt")
                .get()
                .await();
            val trades = snapshot.documents.mapNotNull {
                it.toObject(TradeDto::class.java)?.toDomain()
            }
            emit(trades)
        } catch (e: Exception) {
            throw e;
        }
    }

    override suspend fun addTrade(trade: Trade): Result<String> {
        return try {

            val tradeDto = trade.toDto();
            val docRef = tradesCollection.add(tradeDto).await()
            Result.Success(docRef.id);
        } catch (e: Exception) {
            Result.Error(e.toString());
        }
    }

    override fun getRecentTrades(userId: String, fromMilis: Long, limit: Int): Flow<List<Trade>> = callbackFlow {
        val query = tradesCollection.whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("createdAt", fromMilis)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())

        val listener = query.addSnapshotListener { snapshot, error ->
            if(error != null) {
                close (error)
                return@addSnapshotListener
            }
            val trades = snapshot?.documents?.mapNotNull {
                it.toObject(TradeDto::class.java)?.toDomain()
            } ?: emptyList()
                trySend(trades)
            }
        awaitClose { listener.remove() }


    }
}