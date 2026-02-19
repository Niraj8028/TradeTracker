package com.wallstreet.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import com.wallstreet.R

sealed class BottomNavItem(
    val key: NavKey,
    val label: Int,
    val icon: ImageVector
) {
    data object Journal : BottomNavItem(
        key = AppRoute.Home.TradeHistoryKey,
        label = R.string.nav_journal,
        icon = Icons.Filled.Person
    )

    data object Analytics : BottomNavItem(
        key = AppRoute.Home.EquityMetricsKey,
        label = R.string.nav_analytics,
        icon = Icons.Filled.Person
    )

    data object Strategies : BottomNavItem(
        key = AppRoute.Home.StrategiesKey,
        label = R.string.nav_strategies,
        icon = Icons.Filled.Person
    )

    data object Profile : BottomNavItem(
        key = AppRoute.Home.ProfileKey,
        label = R.string.nav_profile,
        icon = Icons.Filled.Person
    )

    companion object {
        val items = listOf(Journal, Analytics, Strategies, Profile)
    }
}