package com.wallstreet.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.wallstreet.presentation.analytics.AnalyticsScreen
import com.wallstreet.presentation.components.AppBottomBar
import com.wallstreet.presentation.home.HomeScreen
import com.wallstreet.presentation.log_trade.LogTradeScreen
import com.wallstreet.presentation.profile.ProfileScreen
import com.wallstreet.presentation.profile.screens.DeleteAccountScreen
import com.wallstreet.presentation.profile.screens.PrivacyPolicyScreen
import com.wallstreet.presentation.profile.screens.SecurityPrivacyScreen
import com.wallstreet.presentation.profile.screens.TermsOfServiceScreen
import com.wallstreet.presentation.strategy.StrategiesScreen
import com.wallstreet.presentation.strategy.detail.StrategyDetailsScreen
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Composable
fun HomeNavigation(
    onLogout: () -> Unit,
    initialRoute: NavKey? = null,
    modifier: Modifier = Modifier
) {

    val slideFromRight = remember { mutableStateOf(true) }
    val tabKeys = remember { BottomNavItem.items.map { it.key }.toSet() }

    val homeBackStack = rememberNavBackStack(
        configuration = SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(
                        AppRoute.Home.DashboardRoute::class,
                        AppRoute.Home.DashboardRoute.serializer()
                    )
                    subclass(
                        AppRoute.Home.TradeHistoryRoute::class,
                        AppRoute.Home.TradeHistoryRoute.serializer()
                    )
                    subclass(
                        AppRoute.Home.LogTradeRoute::class,
                        AppRoute.Home.LogTradeRoute.serializer()
                    )
                    subclass(
                        AppRoute.Home.StrategiesRoute::class,
                        AppRoute.Home.StrategiesRoute.serializer()
                    )
                    subclass(
                        AppRoute.Home.ProfileRoute::class,
                        AppRoute.Home.ProfileRoute.serializer()
                    )
                    subclass(
                        AppRoute.Home.EquityMetricsRoute::class,
                        AppRoute.Home.EquityMetricsRoute.serializer()
                    )
                    subclass(
                        AppRoute.Home.MistakeAnalysisRoute::class,
                        AppRoute.Home.MistakeAnalysisRoute.serializer()
                    )
                    subclass(
                        AppRoute.Home.CalendarRoute::class,
                        AppRoute.Home.CalendarRoute.serializer()
                    )
                    subclass(
                        AppRoute.Home.JournalDetailRoute::class,
                        AppRoute.Home.JournalDetailRoute.serializer()
                    )
                    subclass(
                        AppRoute.Home.StrategyDetailRoute::class,
                        AppRoute.Home.StrategyDetailRoute.serializer()
                    )
                    subclass(
                        AppRoute.Home.SecurityPrivacyRoute::class,
                        AppRoute.Home.SecurityPrivacyRoute.serializer()
                    )
                    subclass(
                        AppRoute.Home.PrivacyPolicyRoute::class,
                        AppRoute.Home.PrivacyPolicyRoute.serializer()
                    )
                    subclass(
                        AppRoute.Home.TermsOfServiceRoute::class,
                        AppRoute.Home.TermsOfServiceRoute.serializer()
                    )
                    subclass(
                        AppRoute.Home.DeleteAccountRoute::class,
                        AppRoute.Home.DeleteAccountRoute.serializer()
                    )
                    subclass(
                        AppRoute.Home.AnalyticsRoute::class,
                        AppRoute.Home.AnalyticsRoute.serializer()
                    )
                }
            }
        },
        AppRoute.Home.DashboardRoute
    )

    // Handle initial route if provided (e.g. from notification)
    LaunchedEffect(initialRoute) {
        initialRoute?.let {
            if (homeBackStack.last() != it) {
                homeBackStack.add(it)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            HomeBottomBar(homeBackStack, tabKeys, slideFromRight)
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            NavDisplay(
                backStack = homeBackStack,
                onBack = {
                    if (homeBackStack.size > 1) {
                        homeBackStack.removeLastOrNull()
                    }
                },
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator()
                ),
                transitionSpec = {
                    if (slideFromRight.value) {
                        slideInHorizontally(
                            animationSpec = tween(300, easing = FastOutSlowInEasing),
                            initialOffsetX = { it }
                        ) togetherWith slideOutHorizontally(
                            animationSpec = tween(300, easing = FastOutSlowInEasing),
                            targetOffsetX = { -it / 4 }
                        )
                    } else {
                        slideInHorizontally(
                            animationSpec = tween(300, easing = FastOutSlowInEasing),
                            initialOffsetX = { -it }
                        ) togetherWith slideOutHorizontally(
                            animationSpec = tween(300, easing = FastOutSlowInEasing),
                            targetOffsetX = { it / 4 }
                        )
                    }
                },
                popTransitionSpec = {
                    slideInHorizontally(
                        animationSpec = tween(300, easing = FastOutSlowInEasing),
                        initialOffsetX = { -it / 4 }
                    ) togetherWith slideOutHorizontally(
                        animationSpec = tween(300, easing = FastOutSlowInEasing),
                        targetOffsetX = { it }
                    )
                },
                predictivePopTransitionSpec = {
                    slideInHorizontally(
                        animationSpec = tween(300, easing = FastOutSlowInEasing),
                        initialOffsetX = { -it / 4 }
                    ) togetherWith slideOutHorizontally(
                        animationSpec = tween(300, easing = FastOutSlowInEasing),
                        targetOffsetX = { it }
                    )
                },

                entryProvider = entryProvider {

                    // ---- Bottom nav tabs ----

                    entry<AppRoute.Home.DashboardRoute> {
                        HomeScreen()
                    }
                    entry<AppRoute.Home.AnalyticsRoute> {
                        AnalyticsScreen()
                    }
                    entry<AppRoute.Home.TradeHistoryRoute> {
                        Text("Trade History")
                    }
                    entry<AppRoute.Home.StrategiesRoute> {
                        Text("Strategies")
                    }
                    entry<AppRoute.Home.EquityMetricsRoute> {
                        Text("Equity Metrics")
                    }
                    entry<AppRoute.Home.ProfileRoute> {
                        ProfileScreen(
                            onLogout = onLogout,
                            onSecurityPrivacy = { homeBackStack.add(AppRoute.Home.SecurityPrivacyRoute) },
                            onPrivacyPolicy = { homeBackStack.add(AppRoute.Home.PrivacyPolicyRoute) },
                            onTermsOfService = { homeBackStack.add(AppRoute.Home.TermsOfServiceRoute) },
                            onDeleteAccount = { homeBackStack.add(AppRoute.Home.DeleteAccountRoute) }
                        )
                    }
                    entry<AppRoute.Home.CalendarRoute> {
                        StrategiesScreen(
                            onStrategyClick = { strategyId ->
                                homeBackStack.add(AppRoute.Home.StrategyDetailRoute(strategyId))
                            },
                            onAddStrategy = { },
                        )
                    }

                    // ---- Push screens (no bottom bar) ----

                    entry<AppRoute.Home.LogTradeRoute> {
                        LogTradeScreen(
                            onNavigateBack = {
                                if (homeBackStack.size > 1) {
                                    homeBackStack.removeLastOrNull()
                                }
                            },
                            onNavigateToHome = {
                                while (homeBackStack.size > 1) {
                                    homeBackStack.removeLastOrNull()
                                }
                            }
                        )
                    }
                    entry<AppRoute.Home.MistakeAnalysisRoute> {
                        Text("Mistake Analysis")
                    }
                    entry<AppRoute.Home.JournalDetailRoute> { key ->
                        Text("Journal: ${key.tradeId}")
                    }
                    entry<AppRoute.Home.StrategyDetailRoute> { key ->
                        StrategyDetailsScreen(
                            strategyId = key.strategyId,
                            onBack = {
                                if (homeBackStack.size > 1) {
                                    homeBackStack.removeLastOrNull()
                                }
                            }
                        )
                    }
                    entry<AppRoute.Home.SecurityPrivacyRoute> {
                        SecurityPrivacyScreen(onBack = {
                            if (homeBackStack.size > 1) {
                                homeBackStack.removeLastOrNull()
                            }
                        })
                    }
                    entry<AppRoute.Home.PrivacyPolicyRoute> {
                        PrivacyPolicyScreen(onBack = {
                            if (homeBackStack.size > 1) {
                                homeBackStack.removeLastOrNull()
                            }
                        })
                    }
                    entry<AppRoute.Home.TermsOfServiceRoute> {
                        TermsOfServiceScreen(onBack = {
                            if (homeBackStack.size > 1) {
                                homeBackStack.removeLastOrNull()
                            }
                        })
                    }
                    entry<AppRoute.Home.DeleteAccountRoute> {
                        DeleteAccountScreen(
                            onBack = {
                                if (homeBackStack.size > 1) {
                                    homeBackStack.removeLastOrNull()
                                }
                            },
                            onDelete = onLogout
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun HomeBottomBar(
    backStack: NavBackStack<NavKey>,
    tabKeys: Set<NavKey>,
    slideFromRight: MutableState<Boolean>
) {
    val currentKey by remember { derivedStateOf { backStack.last() } }

    if (currentKey !in tabKeys) {
        Spacer(modifier = Modifier.fillMaxWidth().navigationBarsPadding())
        return
    }

    AppBottomBar(
        currentKey = currentKey,
        onItemClick = { key ->
            if (backStack.last() != key) {
                val currentIndex = BottomNavItem.items.indexOfFirst { it.key == backStack.last() }
                val targetIndex = BottomNavItem.items.indexOfFirst { it.key == key }
                slideFromRight.value = targetIndex > currentIndex

                while (backStack.size > 1 && backStack.last() in tabKeys) {
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
