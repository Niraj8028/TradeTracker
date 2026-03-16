package com.wallstreet.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
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
        key = AppRoute.Home.DashboardRoute,
        label = R.string.nav_journal,
        icon = Icons.Filled.Home
    )

    data object Analytics : BottomNavItem(
        key = AppRoute.Home.StrategiesRoute,
        label = R.string.nav_analytics,
        icon = Icons.Filled.AutoGraph
    )

    data object LogTrade : BottomNavItem(
        key = AppRoute.Home.LogTradeRoute,
        label = R.string.nav_log_trade,
        icon = Icons.Filled.AutoGraph
    )

    data object Strategies : BottomNavItem(
        key = AppRoute.Home.CalendarRoute,
        label = R.string.nav_strategies,
        icon = Icons.Filled.CalendarMonth
    )

    data object Profile : BottomNavItem(
        key = AppRoute.Home.ProfileRoute,
        label = R.string.nav_profile,
        icon = Icons.Filled.Person
    )

    companion object {
        val items = listOf(Journal, Analytics, Strategies, Profile)
    }
}