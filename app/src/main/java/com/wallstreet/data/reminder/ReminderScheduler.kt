package com.wallstreet.data.reminder

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.wallstreet.data.sync.TradeReminderWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

class ReminderScheduler(private val workManager: WorkManager) {

    fun scheduleDailyReminder() {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        // Set to 8:00 PM
        calendar.set(Calendar.HOUR_OF_DAY, 20)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        if (calendar.timeInMillis <= now) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val initialDelay = calendar.timeInMillis - now

        val request = PeriodicWorkRequestBuilder<TradeReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "trade_reminder_daily",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
