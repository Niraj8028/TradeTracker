// file: com.wallstreet.core.splash.SplashGate.kt
package com.wallstreet.core.splash

sealed interface StartDestination {
    data object Unknown : StartDestination
    data object Home : StartDestination
    data object Auth : StartDestination
    data object Otp : StartDestination
    data object Onboarding : StartDestination

}