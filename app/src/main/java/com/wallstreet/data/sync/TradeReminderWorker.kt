package com.wallstreet.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.wallstreet.core.util.NotificationHelper
import com.wallstreet.domain.usecase.trade.CheckTodayTradeUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TradeReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val checkTodayTradeUseCase: CheckTodayTradeUseCase by inject()

    override suspend fun doWork(): Result {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return Result.success()

        val hasTrade = checkTodayTradeUseCase(userId)

        if (!hasTrade) {
            NotificationHelper.showTradeReminderNotification(applicationContext)
        }

        return Result.success()
    }
}
