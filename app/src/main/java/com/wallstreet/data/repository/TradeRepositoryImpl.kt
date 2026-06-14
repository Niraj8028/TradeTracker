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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import com.wallstreet.core.perf.withTrace
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

    override suspend fun addTrade(trade: Trade): Result<String> =
        withTrace("trade_add") { trace ->
            try {
                val entity = trade.toEntity()
                tradeDao.insertTrade(entity)
                syncScheduler.scheduleSync(trade.userId)
                trace.putAttribute("result", "success")
                Result.Success(entity.id)
            } catch (e: Exception) {
                trace.putAttribute("result", "error")
                Result.Error(e.toString())
            }
        }

    override fun getRecentTrades(userId: String, fromMilis: Long, limit: Int): Flow<List<Trade>> =
        tradeDao.getAllTrades(userId).map { entities ->
            entities.map { it.toDomain() }
                .filter { it.tradeDate >= fromMilis }
                .take(limit)
        }

    override suspend fun syncPendingTrades(userId: String): Boolean =
        withTrace("trade_sync_pending") { trace ->
            val pending = tradeDao.getPendingSyncTrades(userId)
            if (pending.isEmpty()) {
                trace.putAttribute("pending_count", "0")
                return@withTrace true
            }

            trace.putAttribute("pending_count", pending.size.toString())
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
            trace.putAttribute("result", if (allSucceeded) "success" else "partial_failure")
            allSucceeded
        }

    override suspend fun clearLocalData(userId: String) {
        tradeDao.deleteAllTrades(userId)
    }

    override suspend fun hasPendingTrades(userId: String): Boolean {
        return tradeDao.getPendingSyncTrades(userId).isNotEmpty()
    }

    override suspend fun deleteTrade(tradeId: String): Result<Unit> =
        withTrace("trade_delete") { trace ->
            try {
                tradeDao.deleteTradeById(tradeId)
                tradesCollection.document(tradeId).delete().await()
                trace.putAttribute("result", "success")
                Result.Success(Unit)
            } catch (e: Exception) {
                trace.putAttribute("result", "error")
                Result.Error(e.toString())
            }
        }

    override suspend fun seedFromFirestore(userId: String) =
        withTrace("firestore_seed") { trace ->
            try {
                val snapshot = tradesCollection
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                Timber.d("seedFromFirestore: fetched ${snapshot.documents.size} docs for userId=$userId")
                trace.putAttribute("fetched_count", snapshot.documents.size.toString())

                val pendingIds = tradeDao.getPendingSyncTrades(userId).map { it.id }.toSet()

                snapshot.documents.forEach { doc ->
                    val dto = doc.toObject(TradeDto::class.java) ?: return@forEach
                    if (dto.id !in pendingIds) {
                        tradeDao.insertTrade(dto.toDomain().toEntity(SyncStatus.SYNCED))
                    }
                }
                trace.putAttribute("result", "success")
            } catch (e: Exception) {
                trace.putAttribute("result", "error")
                Timber.e(e, "seedFromFirestore failed for userId=$userId")
            }
        }
}
