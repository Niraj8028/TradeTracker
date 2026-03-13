package com.wallstreet.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.wallstreet.core.splash.StartDestination
import com.wallstreet.presentation.components.AppBottomBar
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Composable
fun AppNavigation(
    startDestination: StartDestination,

    modifier: Modifier = Modifier
) {
    val initialRoute = when (startDestination) {
        StartDestination.Home -> AppRoute.Home
        StartDestination.Auth -> AppRoute.OnBoarding
        StartDestination.Otp -> AppRoute.OnBoarding
        StartDestination.Unknown -> AppRoute.OnBoarding
    }
    val backStack = rememberNavBackStack(
        configuration = SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(AppRoute.OnBoarding::class, AppRoute.OnBoarding.serializer())
                    subclass(AppRoute.Home::class, AppRoute.Home.serializer())
                }
            }
        },
        initialRoute
    )

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {

            // ---------------- AUTH FLOW (NO BOTTOM BAR) ----------------

            entry<AppRoute.OnBoarding> {
                OnboardingNavigation(
                    goToOtp = startDestination == StartDestination.Otp,
                    onLogin = {
                        backStack.remove(AppRoute.OnBoarding)
                        backStack.add(AppRoute.Home)
                    }
                )
            }
            entry<AppRoute.Home> {
                HomeNavigation(
                    onLogout = {
                        backStack.remove(AppRoute.Home)
                        backStack.add(AppRoute.OnBoarding)
                    })
            }

        }
    )
}

@Composable
fun MainScaffold(
    backStack: NavBackStack<NavKey>,
    content: @Composable () -> Unit
) {
    Scaffold(
        bottomBar = {
            AppBottomBar(
                currentKey = backStack.last(),
                onItemClick = { key ->
                    // Don't navigate if we're already on that tab
                    if (backStack.last() != key) {
                        // Remove all Home-level destinations above the root
                        // so tab switches don't stack on top of each other
                        val homeKeys = setOf(
                            AppRoute.Home.DashboardKey,
                            AppRoute.Home.TradeHistoryKey,
                            AppRoute.Home.EquityMetricsKey,
                            AppRoute.Home.StrategiesKey,
                            AppRoute.Home.ProfileKey
                        )
                        // Pop back to AppRoute.Home, then push the selected tab
                        while (backStack.size > 1 && backStack.last() in homeKeys) {
                            backStack.removeLastOrNull()
                        }
                        backStack.add(key)
                    }
                },
                onFabClick = {
                    backStack.add(AppRoute.Home.LogTradeKey)
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            content()
        }
    }
}