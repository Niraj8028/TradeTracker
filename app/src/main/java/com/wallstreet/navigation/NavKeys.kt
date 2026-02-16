package com.wallstreet.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// Auth flow
@Serializable data object SplashKey     : NavKey
@Serializable data object OnboardingKey : NavKey
@Serializable data object LoginKey      : NavKey
@Serializable data object RegisterKey   : NavKey

// Bottom nav destinations
@Serializable data object DashboardKey    : NavKey
@Serializable data object TradeHistoryKey : NavKey
@Serializable data object LogTradeKey     : NavKey
@Serializable data object StrategiesKey   : NavKey
@Serializable data object ProfileKey      : NavKey

// Push screens
@Serializable data object EquityMetricsKey   : NavKey
@Serializable data object MistakeAnalysisKey : NavKey
@Serializable data object CalendarKey        : NavKey

// Screens with arguments
@Serializable data class JournalDetailKey(val tradeId: String)    : NavKey
@Serializable data class StrategyDetailKey(val strategyId: String) : NavKey