package com.wallstreet.core.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_prefs")

class OnboardingPreferences(private val context: Context) {

    private val ONBOARDING_KEY = booleanPreferencesKey("onboarding_completed")

    suspend fun isOnboardingCompleted(): Boolean {
        return context.dataStore.data.first()[ONBOARDING_KEY] ?: false
    }

    suspend fun setOnboardingCompleted() {
        context.dataStore.edit { it[ONBOARDING_KEY] = true }
    }

    suspend fun clearOnboardingCompleted() {
        context.dataStore.edit { it[ONBOARDING_KEY] = false }
    }
}
