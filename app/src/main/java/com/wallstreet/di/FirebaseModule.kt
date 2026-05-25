package com.wallstreet.di

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.wallstreet.data.analytics.FirebaseAnalyticsManagerImpl
import com.wallstreet.domain.analytics.AnalyticsManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val firebaseModule = module {
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
    single { FirebaseAnalytics.getInstance(androidContext()) }
    single { FirebaseCrashlytics.getInstance() }
    single<AnalyticsManager> { FirebaseAnalyticsManagerImpl(get(), get()) }
}