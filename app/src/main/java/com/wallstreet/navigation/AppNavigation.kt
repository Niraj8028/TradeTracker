package com.wallstreet.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
    onLogin: () -> Unit = {},
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isLogoutFlow by rememberSaveable { mutableStateOf(false) }

    val initialRoute = when (startDestination) {
        StartDestination.Home -> AppRoute.Home
        StartDestination.Onboarding -> AppRoute.OnBoarding
        StartDestination.Auth -> AppRoute.OnBoarding
        StartDestination.Otp -> AppRoute.OnBoarding
        StartDestination.Unknown -> AppRoute.OnBoarding
    }

    val skipToLogin = (startDestination == StartDestination.Auth) || isLogoutFlow
    val goToOtp = (startDestination == StartDestination.Otp) && !isLogoutFlow
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
        onBack = {
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
            }
        },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {

            // ---------------- AUTH FLOW (NO BOTTOM BAR) ----------------

            entry<AppRoute.OnBoarding> {
                OnboardingNavigation(
                    skipToLogin = { skipToLogin },
                    goToOtp = { goToOtp },
                    onLogin = {
                        isLogoutFlow = false
                        onLogin()
                        // Add then remove to keep backstack non-empty
                        backStack.add(AppRoute.Home)
                        backStack.remove(AppRoute.OnBoarding)
                    }
                )
            }
            entry<AppRoute.Home> {
                HomeNavigation(
                    onLogout = {
                        isLogoutFlow = true
                        onLogout()
                        // Add then remove to keep backstack non-empty
                        backStack.add(AppRoute.OnBoarding)
                        backStack.remove(AppRoute.Home)
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
                    if (backStack.last() != key) {

                        val homeKeys = setOf(
                            AppRoute.Home.DashboardRoute,
                            AppRoute.Home.TradeHistoryRoute,
                            AppRoute.Home.EquityMetricsRoute,
                            AppRoute.Home.StrategiesRoute,
                            AppRoute.Home.ProfileRoute
                        )

                        while (backStack.size > 1 && backStack.last() in homeKeys) {
                            backStack.removeLastOrNull()
                        }
                        backStack.add(key)
                    }
                },
                onFabClick = {
                    backStack.add(AppRoute.Home.LogTradeRoute)
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            content()
        }
    }
}