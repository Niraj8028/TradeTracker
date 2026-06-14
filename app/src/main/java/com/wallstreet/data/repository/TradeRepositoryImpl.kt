package com.wallstreet.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.wallstreet.core.result.Result
import com.wallstreet.data.local.dao.TradeDao
import com.wallstreet.data.local.entity.SyncStatus
import com.wallstreet.data.mapper.toDomain
import com.wallstreet.data.mapper.toDto
import com.wallstreet.data.mapper.toEntity
import com.wallstreet.data.model.TradeDto
import com.wallstreet.data.remote.FirebaseService
import com.wallstreet.data.sync.SyncScheduler
import com.wallstreet.domain.model.Trade
import com.wallstreet.domain.repository.TradeRepository
import java.util.Calendar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class TradeRepositoryImpl(
    private val tradeDao: TradeDao,
    private val syncScheduler: SyncScheduler,
    private val firestore: FirebaseFirestore
) : TradeRepository {

    private val tradesCollection = firestore.collection(FirebaseService.Collections.TRADES)

    override fun getAllTrades(userId: String): Flow<List<Trade>> =
        tradeDao.getAllTrades(userId)
            .map { entities -> entities.map { it.toDomain() } }

    override suspend fun addTrade(trade: Trade): Result<String> {
        return try {
            val entity = trade.toEntity()
            tradeDao.insertTrade(entity)
            syncScheduler.scheduleSync(trade.userId)
            Result.Success(entity.id)
        } catch (e: Exception) {
            Result.Error(e.toString())
        }
    }

    override fun getRecentTrades(userId: String, fromMilis: Long, limit: Int): Flow<List<Trade>> =
        tradeDao.getAllTrades(userId).map { entities ->
            entities.map { it.toDomain() }
                .filter { it.tradeDate >= fromMilis }
                .take(limit)
        }

    override suspend fun syncPendingTrades(userId: String): Boolean {
        val pending = tradeDao.getPendingSyncTrades(userId)
        if (pending.isEmpty()) return true

        var allSucceeded = true
        pending.forEach { entity ->
            try {
                val dto = entity.toDomain().toDto()
                tradesCollection.document(entity.id).set(dto).await()
                tradeDao.updateSyncStatus(
                    id = entity.id,
                    status = SyncStatus.SYNCED,
                    timestamp = System.currentTimeMillis(),
                    error = null
                )
            } catch (e: Exception) {
                tradeDao.updateSyncStatus(
                    id = entity.id,
                    status = SyncStatus.FAILED,
                    timestamp = System.currentTimeMillis(),
                    error = e.message
                )
                allSucceeded = false
            }
        }
        return allSucceeded
    }

    override suspend fun clearLocalData(userId: String) {
        tradeDao.deleteAllTrades(userId)
    }

    override suspend fun hasPendingTrades(userId: String): Boolean {
        return tradeDao.getPendingSyncTrades(userId).isNotEmpty()
    }

    override suspend fun hasTradeToday(userId: String): Boolean {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis

        return tradeDao.getTradeCountInRange(userId, startOfDay, endOfDay) > 0
    }

    override suspend fun deleteTrade(tradeId: String): Result<Unit> {
        return try {
            tradeDao.deleteTradeById(tradeId)
            tradesCollection.document(tradeId).delete().await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.toString())
        }
    }

    override suspend fun seedFromFirestore(userId: String) {
        try {
            val snapshot = tradesCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            Timber.d("seedFromFirestore: fetched ${snapshot.documents.size} docs for userId=$userId")

            val pendingIds = tradeDao.getPendingSyncTrades(userId).map { it.id }.toSet()

            snapshot.documents.forEach { doc ->
                val dto = doc.toObject(TradeDto::class.java) ?: return@forEach
                if (dto.id !in pendingIds) {
                    tradeDao.insertTrade(dto.toDomain().toEntity(SyncStatus.SYNCED))
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "seedFromFirestore failed for userId=$userId")
        }
    }
}
