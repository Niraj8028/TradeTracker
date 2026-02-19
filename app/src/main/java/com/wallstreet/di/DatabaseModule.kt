package com.wallstreet.di

import androidx.room.Room
import com.wallstreet.data.local.database.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "wallstreet.db"
        ).build()
    }
    single { get<AppDatabase>().tradeDao() }
    single { get<AppDatabase>().journalDao() }
    single { get<AppDatabase>().strategyDao() }
    single { get<AppDatabase>().mistakeDao() }
}