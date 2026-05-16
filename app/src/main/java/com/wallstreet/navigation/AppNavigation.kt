package com.wallstreet.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.google.firebase.auth.FirebaseAuth
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
        is StartDestination.Otp -> AppRoute.OnBoarding
        StartDestination.Unknown -> AppRoute.OnBoarding
    }

    val skipToLogin = (startDestination == StartDestination.Auth) || isLogoutFlow
    val otpEmail = (startDestination as? StartDestination.Otp)?.email ?: ""

    // Once the user deliberately abandons email verification (taps "Wrong email? Go back"),
    // we must not re-route them back to the OTP screen even if startDestination is still Otp.
    // This flag starts as true only for Otp destinations and is cleared on abandon/logout.
    var goToOtp by rememberSaveable {
        mutableStateOf((startDestination is StartDestination.Otp) && !isLogoutFlow)
    }
    
    val navConfig = remember {
        SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(AppRoute.OnBoarding::class, AppRoute.OnBoarding.serializer())
                    subclass(AppRoute.Home::class, AppRoute.Home.serializer())
                }
            }
        }
    }

    val backStack = rememberNavBackStack(
        navConfig,
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
                    goToOtp = { if (goToOtp) otpEmail else null },
                    onLogin = {
                        isLogoutFlow = false
                        goToOtp = false
                        onLogin()
                        // Add then remove to keep backstack non-empty
                        backStack.add(AppRoute.Home)
                        backStack.remove(AppRoute.OnBoarding)
                    },
                    onLogout = {
                        // Covers both explicit logout AND abandon() from EmailVerificationScreen
                        // (abandon calls FirebaseAuth.signOut() which triggers this path).
                        goToOtp = false
                        isLogoutFlow = true
                        FirebaseAuth.getInstance().signOut()
                        onLogout()
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