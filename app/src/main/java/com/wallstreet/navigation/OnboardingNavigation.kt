package com.wallstreet.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.wallstreet.core.preferences.OnboardingPreferences
import com.wallstreet.presentation.auth.login.LoginScreen
import com.wallstreet.presentation.auth.verification.EmailVerificationScreen
import com.wallstreet.presentation.auth.register.RegisterScreen
import com.wallstreet.presentation.onboarding.OnboardingScreen
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Composable
fun OnboardingNavigation(
    onLogin: () -> Unit,
    goToOtp: () -> Boolean = { false },
    skipToLogin: () -> Boolean = { false },
    modifier: Modifier = Modifier
) {
    val initialRoute = remember {
        when {
            goToOtp() -> AppRoute.OnBoarding.EmailVerificationScreen
            skipToLogin() -> AppRoute.OnBoarding.Login
            else -> AppRoute.OnBoarding.Onboarding
        }
    }


    val onBoardingBackStack = rememberNavBackStack(
        configuration = SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(
                        AppRoute.OnBoarding.Onboarding::class,
                        AppRoute.OnBoarding.Onboarding.serializer()
                    )
                    subclass(
                        AppRoute.OnBoarding.Login::class,
                        AppRoute.OnBoarding.Login.serializer()
                    )
                    subclass(
                        AppRoute.OnBoarding.Register::class,
                        AppRoute.OnBoarding.Register.serializer()
                    )
                    subclass(
                        AppRoute.OnBoarding.EmailVerificationScreen::class,
                        AppRoute.OnBoarding.EmailVerificationScreen.serializer()
                    )
                }
            }
        },
        initialRoute
    )

    // ✅ No LaunchedEffect blocks — they caused the 1-frame flash
    // initialRoute already handles all three cases correctly

    NavDisplay(
        backStack = onBoardingBackStack,
        modifier = modifier,
        onBack = { onBoardingBackStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {

            entry<AppRoute.OnBoarding.Onboarding> {
                val context = LocalContext.current
                OnboardingScreen(
                    onFinish = {
                        // ✅ Remove onboarding from backstack so back button
                        // doesn't return to it after navigating to Login
                        onBoardingBackStack.removeLastOrNull()
                        onBoardingBackStack.add(AppRoute.OnBoarding.Login)
                    }
                )
            }

            entry<AppRoute.OnBoarding.Login> {
                LoginScreen(
                    onLoginSuccess = { onLogin() },
                    onNavigateToRegister = { onBoardingBackStack.add(AppRoute.OnBoarding.Register) },
                    onNavigateToOtp = { onBoardingBackStack.add(AppRoute.OnBoarding.EmailVerificationScreen) }
                )
            }

            entry<AppRoute.OnBoarding.Register> {
                RegisterScreen(
                    onRegisterSuccess = { onLogin() },
                    onNavigateToLogin = { onBoardingBackStack.removeLastOrNull() },
                    onNavigateToOtp = { onBoardingBackStack.add(AppRoute.OnBoarding.EmailVerificationScreen) }
                )
            }

            entry<AppRoute.OnBoarding.EmailVerificationScreen> {
                EmailVerificationScreen(onVerified = { onLogin() })
            }
        }
    )
}