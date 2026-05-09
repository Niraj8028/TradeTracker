package com.wallstreet.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.wallstreet.data.local.dao.TradeDao
import com.wallstreet.data.local.entity.SyncStatus
import com.wallstreet.data.mapper.toDomain
import com.wallstreet.data.mapper.toDto
import com.wallstreet.data.remote.FirebaseService
import kotlinx.coroutines.tasks.await
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val tradeDao: TradeDao by inject()
    private val firestore: FirebaseFirestore by inject()

    private val tradesCollection = firestore.collection(FirebaseService.Collections.TRADES)

    override suspend fun doWork(): Result {
        val userId = inputData.getString(KEY_USER_ID) ?: return Result.failure()
        val pending = tradeDao.getPendingSyncTrades(userId)

        var hasFailed = false
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
                hasFailed = true
            }
        }

        return if (hasFailed) Result.retry() else Result.success()
    }

    companion object {
        const val KEY_USER_ID = "user_id"
    }
}
