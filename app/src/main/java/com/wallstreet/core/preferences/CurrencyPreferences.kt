package com.wallstreet.core.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val currencySymbols: Map<String, String> = mapOf(
    "USD" to "$",
    "INR" to "₹",
    "EUR" to "€",
    "GBP" to "£",
    "JPY" to "¥",
    "AUD" to "A$",
    "CAD" to "C$",
)

class CurrencyPreferences(private val context: Context) {

    private val CURRENCY_KEY = stringPreferencesKey("currency_code")

    val currencyCode: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[CURRENCY_KEY] ?: "USD" }

    val currencySymbol: Flow<String> = currencyCode
        .map { code -> currencySymbols[code] ?: "$" }

    suspend fun setCurrency(code: String) {
        context.dataStore.edit { prefs ->
            prefs[CURRENCY_KEY] = code
        }
    }
}
