package com.wallstreet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.FirebaseAuth
import com.wallstreet.core.splash.SplashGate
import com.wallstreet.core.splash.StartDestination
import com.wallstreet.navigation.AppNavigation
import com.wallstreet.ui.theme.WallStreetAndroidTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        val splashScreen = installSplashScreen()

        // 🔒 Hold XML splash until decision is ready
        splashScreen.setKeepOnScreenCondition {
            !SplashGate.isReady
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 🔍 Decide route immediately (cached auth only)
        decideStartDestination()

        setContent {
            WallStreetAndroidTheme {
                AppNavigation(
                    startDestination = SplashGate.startDestination
                )
            }
        }
    }

    private fun decideStartDestination() {
        val user = FirebaseAuth.getInstance().currentUser

        SplashGate.startDestination =
            if (user != null) {
                StartDestination.Home
            } else {
                StartDestination.Auth
            }

        // 🚀 Release splash
        SplashGate.isReady = true
    }
}