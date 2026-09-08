package com.wallstreet.core.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeTypes(name: String) {
    LIGHT("Light"), DARK("Dark"), SYSTEM("System")
}

class ThemePreferences(private val context: Context) {

    private val THEME_KEY = stringPreferencesKey("theme_mode")

    val theme: Flow<ThemeTypes> = context.dataStore.data
        .map { prefs ->
            when (prefs[THEME_KEY]) {
                ThemeTypes.LIGHT.name -> ThemeTypes.LIGHT
                ThemeTypes.DARK.name -> ThemeTypes.DARK
                ThemeTypes.SYSTEM.name -> ThemeTypes.SYSTEM
                else -> ThemeTypes.SYSTEM // default
            }
        }

    suspend fun setTheme(theme: ThemeTypes) {
        context.dataStore.edit { prefs ->
            prefs[THEME_KEY] = theme.name
        }
    }
}