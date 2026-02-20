package com.wallstreet.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

val appModule = module {
    single { androidContext().dataStore }
}