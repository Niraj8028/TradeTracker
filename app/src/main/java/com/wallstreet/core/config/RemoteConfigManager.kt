package com.wallstreet.core.config

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.wallstreet.BuildConfig
import com.wallstreet.core.constants.AppConstants
import com.wallstreet.domain.model.Strategy
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber

/**
 * Central access point for Firebase Remote Config values.
 *
 * In-app defaults are set immediately so getters always return a sane value even
 * before the first network fetch completes. Call [fetchAndActivate] once at startup
 * to pull the latest server values.
 */
class RemoteConfigManager(
    private val remoteConfig: FirebaseRemoteConfig
) {
    private val json = Json { ignoreUnknownKeys = true }

    init {
        val settings = remoteConfigSettings {
            // Allow instant refresh while developing; throttle to 1h in release.
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0 else 3600
        }
        remoteConfig.setConfigSettingsAsync(settings)
        remoteConfig.setDefaultsAsync(
            mapOf(
                KEY_MISTAKE_TAGS to json.encodeToString(AppConstants.mistakes),
                KEY_DEFAULT_STRATEGIES to DEFAULT_STRATEGIES_JSON
            )
        )
    }

    /** Fetches the latest values from the server and activates them. Safe to ignore failures. */
    suspend fun fetchAndActivate(): Boolean = try {
        val activated = remoteConfig.fetchAndActivate().await()
        Timber.d("Remote config fetchAndActivate: activated=$activated")
        activated
    } catch (e: Exception) {
        Timber.e(e, "Remote config fetch failed")
        false
    }

    /** Mistake tags shown on the Log Trade screen. Falls back to [AppConstants.mistakes]. */
    fun getMistakeTags(): List<String> {
        val raw = remoteConfig.getString(KEY_MISTAKE_TAGS)
        return runCatching { json.decodeFromString<List<String>>(raw) }
            .getOrNull()
            ?.filter { it.isNotBlank() }
            ?.takeIf { it.isNotEmpty() }
            ?: AppConstants.mistakes
    }

    /** Default strategies offered in the Log Trade dropdown (in addition to the user's own). */
    fun getDefaultStrategies(): List<Strategy> {
        val raw = remoteConfig.getString(KEY_DEFAULT_STRATEGIES)
        return runCatching { json.decodeFromString<List<RemoteStrategy>>(raw) }
            .getOrNull()
            ?.filter { it.name.isNotBlank() }
            ?.map { it.toStrategy() }
            ?: emptyList()
    }

    @Serializable
    private data class RemoteStrategy(
        val name: String,
        val description: String = ""
    ) {
        fun toStrategy() = Strategy(
            id = "default_${name.lowercase().replace(" ", "_")}",
            name = name,
            description = description,
            isCustom = false
        )
    }

    companion object {
        const val KEY_MISTAKE_TAGS = "mistake_tags"
        const val KEY_DEFAULT_STRATEGIES = "default_strategies"

        private val DEFAULT_STRATEGIES_JSON = """
            [
              {"name":"Breakout","description":"Enter when price breaks a key level"},
              {"name":"Trend Following","description":"Trade in the direction of the trend"},
              {"name":"Reversal","description":"Catch trend reversals at extremes"},
              {"name":"Support / Resistance","description":"Trade bounces off key levels"},
              {"name":"Scalping","description":"Quick small-profit trades"}
            ]
        """.trimIndent()
    }
}
