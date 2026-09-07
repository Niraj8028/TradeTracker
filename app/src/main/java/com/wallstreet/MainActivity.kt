package com.wallstreet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.wallstreet.core.preferences.OnboardingPreferences
import com.wallstreet.core.preferences.ThemePreferences
import com.wallstreet.core.preferences.ThemeTypes
import com.wallstreet.core.result.Result
import com.wallstreet.core.splash.SplashGate
import com.wallstreet.core.splash.StartDestination
import com.wallstreet.domain.analytics.AnalyticsManager
import com.wallstreet.domain.repository.UserRepository
import com.wallstreet.data.store.TradeStore
import com.wallstreet.navigation.AppNavigation
import com.wallstreet.ui.theme.WallStreetAndroidTheme

import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val analyticsManager: AnalyticsManager by inject()
    private val tradeStore: TradeStore by inject()
    private val onboardingPreferences: OnboardingPreferences by inject()
    private val userRepository: UserRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Verbose Firestore logging in debug builds only.
        FirebaseFirestore.setLoggingEnabled(BuildConfig.DEBUG)

        val splashScreen = installSplashScreen()

        splashScreen.setKeepOnScreenCondition {
            SplashGate.startDestination.value == null
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            decideStartDestination()
        }

        setContent {
            val context = LocalContext.current
            val themePrefs = remember { ThemePreferences(context) }
            val selectedTheme by themePrefs.theme.collectAsState(initial = ThemeTypes.SYSTEM)
            val isDarkMode = when (selectedTheme) {
                ThemeTypes.DARK -> true
                ThemeTypes.LIGHT -> false
                ThemeTypes.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            WallStreetAndroidTheme(darkTheme = isDarkMode) {
                val destination by SplashGate.startDestination.collectAsState()

                destination?.let {
                    AppNavigation(startDestination = it)
                }
            }
        }
    }

    private suspend fun decideStartDestination() {
        val user = FirebaseAuth.getInstance().currentUser
        try {
            user?.reload()?.await() // Properly await the refresh
        } catch (e: Exception) {
            // If reload fails (e.g. no network), we still proceed with cached state
        }

        user?.let {
            analyticsManager.setUserId(it.uid)
        }


        val destination = when {
            user == null -> StartDestination.Auth
            !user.isEmailVerified -> StartDestination.Otp(user.email ?: "")
            hasCompletedOnboarding(user.uid) -> StartDestination.Home
            else -> StartDestination.Onboarding
        }

        SplashGate.resolve(destination)
    }

    /**
     * The local cache is only authoritative when it says "done". If it doesn't, fall back to
     * the account's Firestore doc — the source of truth — so an app update or a device switch
     * (where the cache was never seeded) doesn't force a completed user back through onboarding.
     * A failed read is treated as "done": re-running onboarding would overwrite the account
     * currency, and the gate re-checks on the next launch anyway.
     */
    private suspend fun hasCompletedOnboarding(uid: String): Boolean {
        if (onboardingPreferences.isOnboardingCompleted(uid)) return true
        return when (val prefs = userRepository.getAccountPrefs(uid)) {
            is Result.Success -> {
                if (prefs.data.onboardingCompleted) {
                    onboardingPreferences.setOnboardingCompleted(uid)
                    true
                } else {
                    false
                }
            }
            else -> true
        }
    }
}