package com.wallstreet.core.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_prefs")

class OnboardingPreferences(private val context: Context) {

    private val ONBOARDING_KEY = booleanPreferencesKey("onboarding_completed")
    private val USER_TYPE_KEY = stringSetPreferencesKey("user_roles")

    suspend fun isOnboardingCompleted(): Boolean {
        return context.dataStore.data.first()[ONBOARDING_KEY] ?: false
    }

    suspend fun setOnboardingCompleted() {
        context.dataStore.edit { it[ONBOARDING_KEY] = true }
    }

    suspend fun saveUserRoles(roles: Set<String>) {
        try {
            context.dataStore.edit { it[USER_TYPE_KEY] = roles }
        } catch (e: Exception) {
            // Silently fail or log. Since it's not compulsory, we don't want to block the user.
            e.printStackTrace()
        }
    }

    fun getUserRoles(): Flow<Set<String>> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { it[USER_TYPE_KEY] ?: emptySet() }
    }
}