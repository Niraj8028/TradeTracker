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
        key = TradeHistoryKey,
        label = R.string.nav_journal ,
        icon = Icons.Filled.Person
    )

    data object Analytics : BottomNavItem(
        key = EquityMetricsKey,
        label = R.string.nav_analytics,
        icon = Icons.Filled.Person
    )

    data object Strategies : BottomNavItem(
        key = StrategiesKey,
        label= R.string.nav_strategies,
        icon = Icons.Filled.Person
    )

    data object Profile : BottomNavItem(
        key = ProfileKey,
        label=  R.string.nav_profile,
        icon = Icons.Filled.Person
    )

    companion object {
        // FAB (Log Trade) is not in this list — it sits in the center
        // and is handled separately in AppBottomBar.kt
        val items = listOf(Journal, Analytics, Strategies, Profile)
    }
}