package com.wallstreet

import android.app.Application
 import com.wallstreet.di.appModule
import com.wallstreet.di.databaseModule
import com.wallstreet.di.firebaseModule
import com.wallstreet.di.repositoryModule
import com.wallstreet.di.useCaseModule
import com.wallstreet.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber

class TradeTrack: Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())

        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@TradeTrack)
            modules(
                appModule,
                firebaseModule,
                databaseModule,
                repositoryModule,
                useCaseModule,
                viewModelModule
            )
        }
    }
}