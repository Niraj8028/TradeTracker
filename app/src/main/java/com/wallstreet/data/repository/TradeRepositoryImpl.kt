package com.wallstreet.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.wallstreet.core.result.Result
import com.wallstreet.data.mapper.toDomain
import com.wallstreet.data.mapper.toDto
import com.wallstreet.data.model.TradeDto
import com.wallstreet.data.remote.FirebaseService
import com.wallstreet.domain.model.Trade
import com.wallstreet.domain.repository.TradeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class TradeRepositoryImpl(
    private val firestore: FirebaseFirestore
): TradeRepository {

    private val tradesCollection = firestore.collection(FirebaseService.Collections.TRADES)

    override suspend fun getAllTrades(userId: String): Result<List<Trade>> {
        return try {
            val snapshot = tradesCollection
                .whereEqualTo("userId", userId)
                .orderBy("createdAt")
                .get()
                .await();
            val trades = snapshot.documents.mapNotNull {
                it.toObject(TradeDto::class.java)?.toDomain()
            }
            Result.Success(trades)
        } catch (e: Exception) {
            Result.Error(e.toString())
        }
    }

    override suspend fun getTradeById(tradeId: String): Result<Trade> {
        TODO("Not yet implemented")
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

    override suspend fun updateTrade(trade: Trade): Result<String> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteTrade(tradeId: String): Result<String> {
        TODO("Not yet implemented")
    }

    override fun observeTrades(userId: String): Flow<List<Trade>> {
        TODO("Not yet implemented")
    }
}