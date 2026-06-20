package com.wallstreet

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.initialize
import com.wallstreet.core.config.RemoteConfigManager
import com.wallstreet.core.logging.CrashlyticsTree
import com.wallstreet.di.appModule
import com.wallstreet.di.databaseModule
import com.wallstreet.di.firebaseModule
import com.wallstreet.di.repositoryModule
import com.wallstreet.di.useCaseModule
import com.wallstreet.di.viewModelModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber

class TradeTrack: Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsTree())
        }

        val koinApp = startKoin {
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

        // Pull the latest Remote Config values; getters fall back to in-app defaults until this completes.
        CoroutineScope(Dispatchers.IO).launch {
            koinApp.koin.get<RemoteConfigManager>().fetchAndActivate()
        }
    }
}