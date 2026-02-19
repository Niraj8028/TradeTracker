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
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Composable
fun HomeNavigation(modifier: Modifier = Modifier) {

    val homeBackStack = rememberNavBackStack(
        configuration = SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(AppRoute.Home.DashboardKey::class,      AppRoute.Home.DashboardKey.serializer())
                    subclass(AppRoute.Home.TradeHistoryKey::class,   AppRoute.Home.TradeHistoryKey.serializer())
                    subclass(AppRoute.Home.LogTradeKey::class,       AppRoute.Home.LogTradeKey.serializer())
                    subclass(AppRoute.Home.StrategiesKey::class,     AppRoute.Home.StrategiesKey.serializer())
                    subclass(AppRoute.Home.ProfileKey::class,        AppRoute.Home.ProfileKey.serializer())
                    subclass(AppRoute.Home.EquityMetricsKey::class,  AppRoute.Home.EquityMetricsKey.serializer())
                    subclass(AppRoute.Home.MistakeAnalysisKey::class,AppRoute.Home.MistakeAnalysisKey.serializer())
                    subclass(AppRoute.Home.CalendarKey::class,       AppRoute.Home.CalendarKey.serializer())
                    subclass(AppRoute.Home.JournalDetailKey::class,  AppRoute.Home.JournalDetailKey.serializer())
                    subclass(AppRoute.Home.StrategyDetailKey::class, AppRoute.Home.StrategyDetailKey.serializer())
                }
            }
        },
        AppRoute.Home.DashboardKey
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
        }
,

        entryProvider = entryProvider {

            // ---- Bottom nav tabs (with bottom bar) ----

            entry<AppRoute.Home.DashboardKey> {
                MainScaffold(homeBackStack) {
                    Text("Dashboard")
                }
            }

            entry<AppRoute.Home.TradeHistoryKey> {
                MainScaffold(homeBackStack) {
                    Text("Trade History")
                }
            }

            entry<AppRoute.Home.StrategiesKey> {
                MainScaffold(homeBackStack) {
                    Text("Strategies")
                }
            }

            entry<AppRoute.Home.EquityMetricsKey> {
                MainScaffold(homeBackStack) {
                    Text("Equity Metrics")
                }
            }

            entry<AppRoute.Home.ProfileKey> {
                MainScaffold(homeBackStack) {
                    Text("Profile")
                }
            }

            // ---- Push screens (no bottom bar) ----

            entry<AppRoute.Home.LogTradeKey> {
                Text("Log Trade")
            }

            entry<AppRoute.Home.MistakeAnalysisKey> {
                Text("Mistake Analysis")
            }

            entry<AppRoute.Home.CalendarKey> {
                Text("Calendar")
            }

            entry<AppRoute.Home.JournalDetailKey> { key ->
                Text("Journal: ${key.tradeId}")
            }

            entry<AppRoute.Home.StrategyDetailKey> { key ->
                Text("Strategy: ${key.strategyId}")
            }
        }
    )
}