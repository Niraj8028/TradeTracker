package com.wallstreet.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable


@Serializable
sealed interface AppRoute : NavKey {
// Auth flow

    @Serializable
    data object OnBoarding : AppRoute, NavKey {
        @Serializable
        data object Splash : NavKey

        @Serializable
        data object Onboarding  : NavKey



        @Serializable
        data object Login : NavKey

        @Serializable
        data object Register : NavKey

        @Serializable
        data object OtpScreen : NavKey
    }

// Bottom nav destinations

    @Serializable
    data object Home : AppRoute, NavKey {

        @Serializable
        data object DashboardRoute : NavKey

        @Serializable
        data object TradeHistoryRoute : NavKey

        @Serializable
        data object LogTradeRoute : NavKey

        @Serializable
        data object StrategiesRoute : NavKey

        @Serializable
        data object ProfileRoute : NavKey

        // Push screens
        @Serializable
        data object EquityMetricsRoute : NavKey

        @Serializable
        data object MistakeAnalysisRoute : NavKey

        @Serializable
        data object CalendarRoute : NavKey

        // Screens with arguments
        @Serializable
        data class JournalDetailRoute(val tradeId: String) : NavKey

        @Serializable
        data class StrategyDetailRoute(val strategyId: String) : NavKey
    }
}