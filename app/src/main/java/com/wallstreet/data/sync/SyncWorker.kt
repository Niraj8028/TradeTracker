package com.wallstreet.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.wallstreet.domain.repository.TradeRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val tradeRepository: TradeRepository by inject()

    override suspend fun doWork(): Result {
        val userId = inputData.getString(KEY_USER_ID) ?: return Result.failure()
        val allSynced = tradeRepository.syncPendingTrades(userId)
        return if (allSynced) Result.success() else Result.retry()
    }

    companion object {
        const val KEY_USER_ID = "user_id"
    }
}
