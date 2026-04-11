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
import com.wallstreet.core.preferences.OnboardingPreferences
import com.wallstreet.core.preferences.ThemePreferences
import com.wallstreet.core.splash.SplashGate
import com.wallstreet.core.splash.StartDestination
import com.wallstreet.data.store.TradeStore
import com.wallstreet.navigation.AppNavigation
import com.wallstreet.ui.theme.WallStreetAndroidTheme

import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val tradeStore: TradeStore by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
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
            val isDarkMode by themePrefs.isDarkMode.collectAsState(initial = false)

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
        val prefs = OnboardingPreferences(applicationContext)
        val onboardingDone = prefs.isOnboardingCompleted()

        val destination = when {
            user == null && !onboardingDone -> StartDestination.Onboarding
            user == null && onboardingDone -> StartDestination.Auth
//            user != null && !user.isEmailVerified -> StartDestination.Otp
            else -> {
                tradeStore.startObserving(user!!.uid)
                StartDestination.Home
            }
        }

        SplashGate.resolve(destination)
    }
}