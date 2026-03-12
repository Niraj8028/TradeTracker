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
        data object DashboardKey : NavKey

        @Serializable
        data object TradeHistoryKey : NavKey

        @Serializable
        data object LogTradeKey : NavKey

        @Serializable
        data object StrategiesKey : NavKey

        @Serializable
        data object ProfileKey : NavKey

        // Push screens
        @Serializable
        data object EquityMetricsKey : NavKey

        @Serializable
        data object MistakeAnalysisKey : NavKey

        @Serializable
        data object CalendarKey : NavKey

        // Screens with arguments
        @Serializable
        data class JournalDetailKey(val tradeId: String) : NavKey

        @Serializable
        data class StrategyDetailKey(val strategyId: String) : NavKey
    }
}