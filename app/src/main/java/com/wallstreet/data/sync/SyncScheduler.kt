package com.wallstreet.data.sync

import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class SyncScheduler(private val workManager: WorkManager) {

    fun scheduleSync(userId: String) {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInputData(
                Data.Builder()
                    .putString(SyncWorker.KEY_USER_ID, userId)
                    .build()
            )
            .build()

        workManager.enqueueUniqueWork(
            "trade_sync_$userId",
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    fun cancelSync(userId: String) {
        workManager.cancelUniqueWork("trade_sync_$userId")
    }
}
