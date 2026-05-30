package com.wallstreet.di

import com.wallstreet.presentation.analytics.AnalyticsViewModel
import com.wallstreet.presentation.auth.login.LoginViewModel
import com.wallstreet.presentation.auth.verification.EmailVerifyViewModel
import com.wallstreet.presentation.auth.register.RegisterViewModel
import com.wallstreet.presentation.profile.ProfileViewModel
import com.wallstreet.presentation.splash.SplashViewModel
import com.wallstreet.presentation.log_trade.LogTradeViewModel
import com.wallstreet.presentation.home.HomeViewModel
import com.wallstreet.presentation.onboarding.OnboardingViewModel
import com.wallstreet.presentation.strategy.StrategyViewModel
import com.wallstreet.presentation.strategy.detail.StrategyDetailViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::SplashViewModel)
    viewModelOf(::OnboardingViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::LogTradeViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::EmailVerifyViewModel)
    viewModelOf(::StrategyViewModel)
    viewModelOf(::AnalyticsViewModel)
    viewModel { (strategyId: String) ->
        StrategyDetailViewModel(
            strategyId = strategyId,
            authRepository = get(),
            getTradesUsecase = get(),
            getStrategyUseCase = get(),
            getHomeStateUsecase = get(),
            getTradeSummaryUseCase = get(),
            getDayPerformanceUseCase = get(),
            equityCurveDataUsecase = get(),
            mistakesAnalysisUsecase = get(),
            symbolPerformanceUsecase = get(),
            getTrendPerformanceUseCase = get()
        )
    }
}