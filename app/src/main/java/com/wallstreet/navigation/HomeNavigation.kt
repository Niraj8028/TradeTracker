package com.wallstreet.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.wallstreet.presentation.home.HomeScreen
import com.wallstreet.presentation.log_trade.LogTradeScreen
import com.wallstreet.presentation.profile.ProfileScreen
import com.wallstreet.presentation.strategy.StrategyScreen
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Composable
fun HomeNavigation(onLogout: () -> Unit, modifier: Modifier = Modifier) {

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
                }
            }
        },
        AppRoute.Home.DashboardRoute
    )

    NavDisplay(
        backStack = homeBackStack,
        modifier = modifier,
        onBack = { homeBackStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        transitionSpec = {
            EnterTransition.None togetherWith ExitTransition.None
        },

        entryProvider = entryProvider {

            // ---- Bottom nav tabs (with bottom bar) ----

            entry<AppRoute.Home.DashboardRoute> {
                MainScaffold(homeBackStack) {
                    HomeScreen()
                }
            }

            entry<AppRoute.Home.TradeHistoryRoute> {
                MainScaffold(homeBackStack) {
                    Text("Trade History")
                }
            }

            entry<AppRoute.Home.StrategiesRoute> {
                MainScaffold(homeBackStack) {
                    Text("Strategies")
                }
            }

            entry<AppRoute.Home.EquityMetricsRoute> {
                MainScaffold(homeBackStack) {
                    Text("Equity Metrics")
                }
            }

            entry<AppRoute.Home.ProfileRoute> {
                MainScaffold(homeBackStack) {
                    ProfileScreen(onLogout = onLogout)
                }
            }

            // ---- Push screens (no bottom bar) ----

            entry<AppRoute.Home.LogTradeRoute> {
                MainScaffold(homeBackStack) {
                    LogTradeScreen()
                }

            }

            entry<AppRoute.Home.MistakeAnalysisRoute> {
                Text("Mistake Analysis")
            }

            entry<AppRoute.Home.CalendarRoute> {
                MainScaffold(homeBackStack) {
                    StrategyScreen()
                }
            }

            entry<AppRoute.Home.JournalDetailRoute> { key ->
                Text("Journal: ${key.tradeId}")
            }

            entry<AppRoute.Home.StrategyDetailRoute> { key ->
                Text("Strategy: ${key.strategyId}")
            }
        }
    )
}