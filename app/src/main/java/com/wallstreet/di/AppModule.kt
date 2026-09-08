package com.wallstreet.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.wallstreet.core.preferences.CurrencyPreferences
import com.wallstreet.core.preferences.OnboardingPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

val appModule = module {
    single { androidContext().dataStore }
    single { OnboardingPreferences(androidContext()) }
    single { CurrencyPreferences(androidContext()) }
}