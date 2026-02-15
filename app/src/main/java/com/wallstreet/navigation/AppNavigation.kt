package com.wallstreet.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.wallstreet.presentation.components.AppBottomBar
import com.wallstreet.presentation.splash.SplashScreen

@Composable
fun AppNavigation() {

    // Own the back stack (official Nav3 pattern)
    val backStack = remember { mutableStateListOf<NavKey>(SplashKey) }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {

            // ---------------- AUTH FLOW (NO BOTTOM BAR) ----------------

            entry<SplashKey> {
                SplashScreen( onSplashComplete = {
                    backStack.removeLastOrNull()
                    backStack.add(DashboardKey)
                })
            }

            entry<OnboardingKey> {
                Text("Onboarding")
            }

            entry<LoginKey> {
                Text("Login")
            }

            entry<RegisterKey> {
                Text("Register")
            }

            // ---------------- MAIN APP (WITH BOTTOM BAR) ----------------

            entry<DashboardKey> {
                MainScaffold(backStack) {
                    Text("Dashboard")
                }
            }

            entry<TradeHistoryKey> {
                MainScaffold(backStack) {
                    Text("Trade History")
                }
            }

            entry<StrategiesKey> {
                MainScaffold(backStack) {
                    Text("Strategies")
                }
            }

            entry<ProfileKey> {
                MainScaffold(backStack) {
                    Text("Profile")
                }
            }

            entry<EquityMetricsKey> {
                MainScaffold(backStack) {
                    Text("Equity Metrics")
                }
            }

            entry<LogTradeKey> {
                MainScaffold(backStack) {
                    Text("Log Trade")
                }
            }

            entry<JournalDetailKey> { key ->
                MainScaffold(backStack) {
                    Text("Journal: ${key.tradeId}")
                }
            }

            entry<StrategyDetailKey> { key ->
                MainScaffold(backStack) {
                    Text("Strategy: ${key.strategyId}")
                }
            }
        }
    )
}

@Composable
fun MainScaffold(
    backStack: SnapshotStateList<NavKey>,
    content: @Composable () -> Unit
) {
    Scaffold(
        bottomBar = {
            AppBottomBar(
                currentKey = backStack.last(),
                onItemClick = { key ->
                    if (backStack.last() != key) {
                        backStack.removeLastOrNull()
                        backStack.add(key)
                    }
                },
                onFabClick = {
                    backStack.add(LogTradeKey)
                }
            )
        }
    ) { padding ->
        Box(modifier = androidx.compose.ui.Modifier.padding(padding)) {
            content()
        }
    }
}
