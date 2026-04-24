package com.wallstreet.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.wallstreet.core.result.Result
import com.wallstreet.data.mapper.toDomain
import com.wallstreet.data.mapper.toDto
import com.wallstreet.data.model.TradeDto
import com.wallstreet.data.remote.FirebaseService
import com.wallstreet.data.store.TradeStore
import com.wallstreet.domain.model.Trade
import com.wallstreet.domain.repository.TradeRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.lang.Error

class TradeRepositoryImpl(
    private val tradeStore: TradeStore,
    private val firestore: FirebaseFirestore
): TradeRepository {

    private val tradesCollection = firestore.collection(FirebaseService.Collections.TRADES)

    override fun getAllTrades(userId: String): Flow<List<Trade>> = tradeStore.trades

    override suspend fun addTrade(trade: Trade): Result<String> {
        return try {
            val tradeDto = trade.toDto();
            val docRef = tradesCollection.add(tradeDto).await()
            Result.Success(docRef.id);
        } catch (e: Exception) {
            Result.Error(e.toString());
        }
    }

    override fun getRecentTrades(userId: String, fromMilis: Long, limit: Int): Flow<List<Trade>> =
        tradeStore.trades.map { trades ->
            trades.filter { it.tradeDate >= fromMilis }
                .take(limit)
        }
}