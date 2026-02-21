package com.wallstreet.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.wallstreet.presentation.auth.login.LoginScreen
import com.wallstreet.presentation.auth.register.RegisterScreen
import com.wallstreet.presentation.onboarding.OnboardingScreen
import com.wallstreet.presentation.splash.SplashScreen
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Composable
fun OnboardingNavigation(
    onLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val onBoardingBackStack = rememberNavBackStack(
        configuration = SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                     subclass(AppRoute.OnBoarding.Onboarding::class,  AppRoute.OnBoarding.Onboarding.serializer())
                    subclass(AppRoute.OnBoarding.Login::class,        AppRoute.OnBoarding.Login.serializer())
                    subclass(AppRoute.OnBoarding.Register::class,     AppRoute.OnBoarding.Register.serializer())
                }
            }
        },
        AppRoute.OnBoarding.Onboarding
    )

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
                OnboardingScreen {
                    onBoardingBackStack.add(AppRoute.OnBoarding.Login)
                }
            }

            entry<AppRoute.OnBoarding.Login> {
                LoginScreen(
                    onLoginSuccess = { onLogin() },
                    onNavigateToRegister = {
                        onBoardingBackStack.add(AppRoute.OnBoarding.Register)
                    }
                )
            }

            entry<AppRoute.OnBoarding.Register> {
                RegisterScreen(
                    onRegisterSuccess = { onLogin() },
                    onNavigateToLogin = {
                        onBoardingBackStack.removeLastOrNull()
                    }
                )
            }
        }
    )
}