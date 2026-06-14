package com.wallstreet

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.wallstreet.core.preferences.OnboardingPreferences
import com.wallstreet.core.preferences.ThemePreferences
import com.wallstreet.core.preferences.ThemeTypes
import com.wallstreet.core.splash.SplashGate
import com.wallstreet.core.splash.StartDestination
import com.wallstreet.core.util.NotificationHelper
import com.wallstreet.domain.analytics.AnalyticsManager
import com.wallstreet.data.store.TradeStore
import com.wallstreet.navigation.AppNavigation
import com.wallstreet.navigation.AppRoute
import androidx.navigation3.runtime.NavKey
import com.wallstreet.ui.theme.WallStreetAndroidTheme
import timber.log.Timber

import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val analyticsManager: AnalyticsManager by inject()
    private val tradeStore: TradeStore by inject()
    private val onboardingPreferences: OnboardingPreferences by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Enable Firestore debug logging
        FirebaseFirestore.setLoggingEnabled(true)

        val splashScreen = installSplashScreen()

        splashScreen.setKeepOnScreenCondition {
            SplashGate.startDestination.value == null
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // next line will use as debug notification
        //NotificationHelper.showTradeReminderNotification(this)
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

                var pendingRoute by remember { mutableStateOf<NavKey?>(null) }

                LaunchedEffect(intent) {
                    val routeStr = intent?.getStringExtra(NotificationHelper.EXTRA_ROUTE)
                    if (routeStr == NotificationHelper.ROUTE_LOG_TRADE) {
                        pendingRoute = AppRoute.Home.LogTradeRoute
                    }
                }

                destination?.let {
                    AppNavigation(
                        startDestination = it,
                        onLogin = { SplashGate.resolve(StartDestination.Home) },
                        onLogout = { SplashGate.resolve(StartDestination.Auth) },
                        initialHomeRoute = pendingRoute
                    )
                }
            }
        }
    }

    private suspend fun decideStartDestination() {
        var user = FirebaseAuth.getInstance().currentUser
        
        // If we have a user, try to reload their state to get the latest emailVerification status.
        // We try up to 3 times with a short delay in case the network is just warming up.
        if (user != null && !user.isEmailVerified) {
            for (i in 1..3) {
                try {
                    user?.reload()?.await()
                    user = FirebaseAuth.getInstance().currentUser // Re-fetch after reload
                    if (user?.isEmailVerified == true) break
                } catch (e: Exception) {
                    Timber.w(e, "MainActivity: reload attempt $i failed")
                }
                if (i < 3) kotlinx.coroutines.delay(500L)
            }
        }

        user?.let {
            analyticsManager.setUserId(it.uid)
        }

        val destination = when {
            user == null -> StartDestination.Auth
            !user.isEmailVerified -> StartDestination.Otp(user.email ?: "")
            !onboardingPreferences.isOnboardingCompleted() -> StartDestination.Onboarding
            else -> {
                StartDestination.Home
            }
        }

        SplashGate.resolve(destination)
    }
}
