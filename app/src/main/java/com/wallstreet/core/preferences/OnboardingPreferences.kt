package com.wallstreet.core.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_prefs")

/**
 * Local, per-account cache of "this user finished onboarding". The source of truth is the
 * user's Firestore doc (see UserRepository.isOnboardingCompleted); this just avoids a network
 * round-trip on subsequent logins on the same install.
 */
class OnboardingPreferences(private val context: Context) {

    private fun key(userId: String) = booleanPreferencesKey("onboarding_completed_$userId")

    suspend fun isOnboardingCompleted(userId: String): Boolean {
        if (userId.isBlank()) return false
        return context.dataStore.data.first()[key(userId)] ?: false
    }

    suspend fun setOnboardingCompleted(userId: String) {
        if (userId.isBlank()) return
        context.dataStore.edit { it[key(userId)] = true }
    }
}
