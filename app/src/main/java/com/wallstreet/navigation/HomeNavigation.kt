package com.wallstreet.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.wallstreet.presentation.analytics.AnalyticsScreen
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
fun HomeNavigation(onLogout: () -> Unit, modifier: Modifier = Modifier) {

    val slideFromRight = remember { mutableStateOf(true) }

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

    NavDisplay(
        backStack = homeBackStack,
        modifier = modifier,
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

            // ---- Bottom nav tabs (with bottom bar) ----

            entry<AppRoute.Home.DashboardRoute> {
                MainScaffold(homeBackStack, onTabNavigation = { slideFromRight.value = it }) {
                    HomeScreen()
                }
            }
            entry<AppRoute.Home.AnalyticsRoute> {
                MainScaffold(homeBackStack, onTabNavigation = { slideFromRight.value = it }) {
                    AnalyticsScreen()
                }
            }
            entry<AppRoute.Home.TradeHistoryRoute> {
                MainScaffold(homeBackStack, onTabNavigation = { slideFromRight.value = it }) {
                    Text("Trade History")
                }
            }

            entry<AppRoute.Home.StrategiesRoute> {
                MainScaffold(homeBackStack, onTabNavigation = { slideFromRight.value = it }) {
                    Text("Strategies")
                }
            }

            entry<AppRoute.Home.EquityMetricsRoute> {
                MainScaffold(homeBackStack, onTabNavigation = { slideFromRight.value = it }) {
                    Text("Equity Metrics")
                }
            }

            entry<AppRoute.Home.ProfileRoute> {
                MainScaffold(homeBackStack, onTabNavigation = { slideFromRight.value = it }) {
                    ProfileScreen(
                        onLogout = onLogout,
                        onSecurityPrivacy = { homeBackStack.add(AppRoute.Home.SecurityPrivacyRoute) },
                        onPrivacyPolicy = { homeBackStack.add(AppRoute.Home.PrivacyPolicyRoute) },
                        onTermsOfService = { homeBackStack.add(AppRoute.Home.TermsOfServiceRoute) },
                        onDeleteAccount = { homeBackStack.add(AppRoute.Home.DeleteAccountRoute) }
                    )
                }
            }

            // ---- Push screens (no bottom bar) ----

            entry<AppRoute.Home.LogTradeRoute> {
                MainScaffold(homeBackStack, onTabNavigation = { slideFromRight.value = it }) {
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
            }

            entry<AppRoute.Home.MistakeAnalysisRoute> {
                Text("Mistake Analysis")
            }

            entry<AppRoute.Home.CalendarRoute> {
                MainScaffold(homeBackStack, onTabNavigation = { slideFromRight.value = it }) {
                    StrategiesScreen(
                        onStrategyClick = { strategyId ->
                            homeBackStack.add(AppRoute.Home.StrategyDetailRoute(strategyId))
                        },
                        onAddStrategy = { },
                    )
                }
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
            entry<AppRoute.Home.DeleteAccountRoute> { key ->
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