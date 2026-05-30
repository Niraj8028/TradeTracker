package com.wallstreet.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.wallstreet.presentation.auth.login.LoginScreen
import com.wallstreet.presentation.auth.verification.EmailVerificationScreen
import com.wallstreet.presentation.auth.register.RegisterScreen
import com.wallstreet.presentation.onboarding.OnboardingScreen
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Composable
fun OnboardingNavigation(
    onLogin: () -> Unit,
    onLogout: () -> Unit = {},
    goToOtp: () -> String? = { null },
    skipToLogin: () -> Boolean = { false },
    modifier: Modifier = Modifier
) {
    val initialRoute = remember {
        val otpEmail = goToOtp()
        when {
            otpEmail != null -> AppRoute.OnBoarding.EmailVerificationScreen(otpEmail)
            skipToLogin() -> AppRoute.OnBoarding.Login
            else -> AppRoute.OnBoarding.Register
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
        onBack = {
            if (onBoardingBackStack.size > 1) {
                onBoardingBackStack.removeLastOrNull()
            }
        },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {

            entry<AppRoute.OnBoarding.Onboarding> {
                OnboardingScreen(
                    onFinish = { onLogin() }
                )
            }

            entry<AppRoute.OnBoarding.Login> {
                LoginScreen(
                    onLoginSuccess = {
                        onBoardingBackStack.add(AppRoute.OnBoarding.Onboarding)
                        onBoardingBackStack.remove(AppRoute.OnBoarding.Login)
                    },
                    onNavigateToRegister = { onBoardingBackStack.add(AppRoute.OnBoarding.Register) },
                    onNavigateToOtp = { email -> 
                        onBoardingBackStack.add(AppRoute.OnBoarding.EmailVerificationScreen(email)) 
                    }
                )
            }

            entry<AppRoute.OnBoarding.Register> {
                RegisterScreen(
                    onRegisterSuccess = {
                        onBoardingBackStack.add(AppRoute.OnBoarding.Onboarding)
                        onBoardingBackStack.remove(AppRoute.OnBoarding.Register)
                    },
                    onNavigateToLogin = {
                        if (onBoardingBackStack.size > 1) {
                            onBoardingBackStack.removeLastOrNull()
                        } else {
                            onBoardingBackStack.add(AppRoute.OnBoarding.Login)
                            onBoardingBackStack.remove(AppRoute.OnBoarding.Register)
                        }
                    },
                    onNavigateToOtp = { email -> 
                        onBoardingBackStack.add(AppRoute.OnBoarding.EmailVerificationScreen(email)) 
                    }
                )
            }

            entry<AppRoute.OnBoarding.EmailVerificationScreen> { route ->
                EmailVerificationScreen(
                    email = route.email,
                    onVerified = {
                        onBoardingBackStack.add(AppRoute.OnBoarding.Onboarding)
                        onBoardingBackStack.remove(route)
                    },
                    onBack = {
                        // viewModel.abandon() in EmailVerify.kt has already:
                        //   - cancelled the polling coroutine
                        //   - deleted the unverified Firebase account
                        //   - signed the user out
                        // So here we only fix the navigation back-stack.

                        if (onBoardingBackStack.size > 1) {
                            // Normal flow: Register → EmailVerification
                            // Popping reveals the Register entry; rememberSaveable keeps form values.
                            onBoardingBackStack.removeLastOrNull()
                        } else {
                            // Cold-start flow: app launched directly into EmailVerification.
                            // Replace this single entry with Register so the user can
                            // correct their email without being redirected back here.
                            // Also notify AppNavigation to clear its goToOtp flag so it
                            // won't re-route to OTP on any future recomposition.
                            onLogout()
                            onBoardingBackStack.add(AppRoute.OnBoarding.Register)
                            onBoardingBackStack.remove(route)
                        }
                    }
                )
            }
        }
    )
}