package com.wallstreet.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import com.wallstreet.presentation.auth.register.RegisterScreen
import com.wallstreet.presentation.auth.verification.EmailVerificationScreen
import com.wallstreet.presentation.auth.welcome.WelcomeScreen
import com.wallstreet.presentation.onboarding.OnboardingScreen
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Composable
fun OnboardingNavigation(
    onLogin: () -> Unit,
    onLogout: () -> Unit = {},
    goToOtp: () -> String? = { null },
    skipToLogin: () -> Boolean = { false },
    needsOnboarding: () -> Boolean = { false },
    modifier: Modifier = Modifier
) {
    // The whole initial back stack is built up front (no LaunchedEffect — those caused a
    // 1-frame flash). Welcome is the stable root for logged-out users; Login/Register are
    // pushed on top of it so Back is always predictable.
    val initialStack: List<NavKey> = remember {
        val otpEmail = goToOtp()
        when {
            otpEmail != null -> listOf(AppRoute.OnBoarding.EmailVerificationScreen(otpEmail))
            // A logout always returns to Login, even if the frozen startDestination still
            // says onboarding is pending (e.g. user finished onboarding then logged out).
            skipToLogin() -> listOf(AppRoute.OnBoarding.Welcome, AppRoute.OnBoarding.Login)
            needsOnboarding() -> listOf(AppRoute.OnBoarding.Onboarding)
            else -> listOf(AppRoute.OnBoarding.Welcome)
        }
    }

    val onBoardingBackStack = rememberNavBackStack(
        configuration = SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(
                        AppRoute.OnBoarding.Welcome::class,
                        AppRoute.OnBoarding.Welcome.serializer()
                    )
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
        *initialStack.toTypedArray()
    )

    /** Swap the current top entry for [route] — used for Login↔Register↔Welcome switches. */
    fun replaceTop(route: NavKey) {
        onBoardingBackStack.removeLastOrNull()
        onBoardingBackStack.add(route)
    }

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
        transitionSpec = {
            slideInHorizontally(
                animationSpec = tween(300, easing = FastOutSlowInEasing),
                initialOffsetX = { it }
            ) togetherWith slideOutHorizontally(
                animationSpec = tween(300, easing = FastOutSlowInEasing),
                targetOffsetX = { -it / 4 }
            )
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

            entry<AppRoute.OnBoarding.Welcome> {
                WelcomeScreen(
                    onSignUp = { onBoardingBackStack.add(AppRoute.OnBoarding.Register) },
                    onLogin = { onBoardingBackStack.add(AppRoute.OnBoarding.Login) }
                )
            }

            entry<AppRoute.OnBoarding.Onboarding> {
                OnboardingScreen(
                    onFinish = { onLogin() }
                )
            }

            entry<AppRoute.OnBoarding.Login> {
                LoginScreen(
                    onLoginSuccess = { onLogin() },
                    onNavigateToRegister = { replaceTop(AppRoute.OnBoarding.Register) },
                    onNavigateToOnboarding = { replaceTop(AppRoute.OnBoarding.Onboarding) },
                    onNavigateToOtp = { email ->
                        onBoardingBackStack.add(AppRoute.OnBoarding.EmailVerificationScreen(email))
                    }
                )
            }

            entry<AppRoute.OnBoarding.Register> {
                RegisterScreen(
                    onRegisterSuccess = { onLogin() },
                    onNavigateToOnboarding = { replaceTop(AppRoute.OnBoarding.Onboarding) },
                    onNavigateToLogin = { replaceTop(AppRoute.OnBoarding.Login) },
                    onNavigateToOtp = { email ->
                        onBoardingBackStack.add(AppRoute.OnBoarding.EmailVerificationScreen(email))
                    }
                )
            }

            entry<AppRoute.OnBoarding.EmailVerificationScreen> { route ->
                EmailVerificationScreen(
                    email = route.email,
                    onVerified = { replaceTop(AppRoute.OnBoarding.Onboarding) },
                    onBack = {
                        // viewModel.abandon() in EmailVerify.kt has already cancelled polling,
                        // deleted the unverified account, and signed out. Here we only fix nav.
                        if (onBoardingBackStack.size > 1) {
                            onBoardingBackStack.removeLastOrNull()
                        } else {
                            // Cold-start flow: app launched straight into verification.
                            onLogout()
                            replaceTop(AppRoute.OnBoarding.Welcome)
                        }
                    }
                )
            }
        }
    )
}
