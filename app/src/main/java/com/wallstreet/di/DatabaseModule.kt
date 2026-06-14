package com.wallstreet.di

import androidx.room.Room
import androidx.work.WorkManager
import com.wallstreet.data.local.database.AppDatabase
import com.wallstreet.data.reminder.ReminderScheduler
import com.wallstreet.data.sync.SyncScheduler
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "tradetrack.db"
        ).addMigrations(AppDatabase.MIGRATION_1_2).build()
    }
    single { get<AppDatabase>().tradeDao() }
    single { WorkManager.getInstance(androidContext()) }
    single { SyncScheduler(get()) }
    single { ReminderScheduler(get()) }
}
