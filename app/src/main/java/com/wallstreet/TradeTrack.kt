package com.wallstreet

import android.app.Application
import com.wallstreet.di.firebaseModule
import com.wallstreet.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class TradeTrack: Application() {
    override fun onCreate() {
        super.onCreate()

        // Start Koin
        startKoin {
            // Log Koin into Android logger
            androidLogger(Level.DEBUG)

            // Reference Android context
            androidContext(this@TradeTrack)

            // Load modules
            modules(
//                appModule,
                firebaseModule,
//                repositoryModule,
                viewModelModule,
            )
        }
    }
}