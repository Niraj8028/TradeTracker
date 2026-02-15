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
import com.wallstreet.presentation.auth.login.LoginScreen
import com.wallstreet.presentation.auth.register.RegisterScreen
import com.wallstreet.presentation.components.AppBottomBar
import com.wallstreet.presentation.onboarding.OnboardingScreen
import com.wallstreet.presentation.profile.ProfileScreen
import com.wallstreet.presentation.profile.User
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
                    backStack.add(OnboardingKey)
                })
            }

            entry<OnboardingKey> {
                OnboardingScreen(onFinish={
                    backStack.removeLastOrNull()
                    backStack.add(LoginKey)
                })
            }

            entry<LoginKey> {
                LoginScreen (
                    onLoginSuccess = {
                        backStack.clear()
                        backStack.add(DashboardKey)
                    },
                    onNavigateToRegister = {
                        backStack.add(RegisterKey)
                    }
                )
            }

            entry<RegisterKey> {
                RegisterScreen(
                    onRegisterSuccess = {
                        backStack.clear()
                        backStack.add(DashboardKey)
                    },
                    onNavigateToLogin = {
                        backStack.removeLastOrNull()  // pops back to Login
                    }
                )
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
//NOTE Tem hardcode user to check of prop handling done here
                val testUser = User(
                    id = "1",
                    name = "Shreyas Damase",
                    email = "shreyas@test.com"
                )
                MainScaffold(backStack) {
                    ProfileScreen(user = testUser)
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
