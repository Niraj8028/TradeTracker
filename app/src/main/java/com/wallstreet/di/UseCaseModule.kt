package com.wallstreet.di

import com.wallstreet.domain.usecase.analytics.GetCalendarDataUseCase
import com.wallstreet.domain.usecase.analytics.GetDayPerformanceUseCase
import com.wallstreet.domain.usecase.analytics.GetOverviewStatsUseCase
import com.wallstreet.domain.usecase.analytics.GetTradeSummaryUseCase
import com.wallstreet.domain.usecase.analytics.GetTrendPerformanceUseCase
import com.wallstreet.domain.usecase.auth.*
import com.wallstreet.domain.usecase.home.ComputeHeatMapDataUsecase
import com.wallstreet.domain.usecase.home.GetEquityCurveDataUsecase
import com.wallstreet.domain.usecase.home.GetHomeStateUsecase
import com.wallstreet.domain.usecase.home.GetMistakesAnalysisUsecase
import com.wallstreet.domain.usecase.home.GetSymbolPerformanceUsecase
import com.wallstreet.domain.usecase.home.RecentTradesDataUsecase
import com.wallstreet.domain.usecase.onboarding.CompleteOnboardingUseCase
import com.wallstreet.domain.usecase.strategy.AddStrategyUseCase
import com.wallstreet.domain.usecase.strategy.DeleteStrategyUseCase
import com.wallstreet.domain.usecase.strategy.GetStrategyStatsUsecase
import com.wallstreet.domain.usecase.strategy.GetStrategyUseCase
import com.wallstreet.domain.usecase.strategy.UpdateStrategyUseCase
import com.wallstreet.domain.usecase.trade.AddTradeUseCase
import com.wallstreet.domain.usecase.trade.GetTradesUseCase
import org.koin.dsl.module

val useCaseModule = module {
    factory { SignInUseCase(get()) }
    factory { SignInWithGoogleUseCase(get()) }
    factory { SignUpUseCase(get(), get()) }
    factory { SignOutUseCase(get()) }
    factory { GetCurrentUserUseCase(get()) }
    factory { VerifyOtpUseCase(get()) }
    factory { ResendVerificationEmailUseCase(get()) }
    factory { SendPasswordResetEmailUseCase(get()) }
    factory { AddTradeUseCase(get()) }
    factory { GetTradesUseCase(get()) }
    factory { GetHomeStateUsecase() }
    factory { ComputeHeatMapDataUsecase() }
    factory { RecentTradesDataUsecase() }
    factory { AddStrategyUseCase(get()) }
    factory { DeleteStrategyUseCase(get()) }
    factory { UpdateStrategyUseCase(get()) }
    factory { GetStrategyUseCase(get()) }
    factory { GetStrategyStatsUsecase(get(), get()) }
    factory { DeleteAccountUseCase(get()) }
    factory { GetEquityCurveDataUsecase() }
    factory { GetMistakesAnalysisUsecase() }
    factory { GetSymbolPerformanceUsecase() }
    factory { GetCalendarDataUseCase(get()) }
    factory { GetDayPerformanceUseCase(get()) }
    factory { GetTradeSummaryUseCase(get()) }
    factory { GetOverviewStatsUseCase(get()) }
    factory { GetTrendPerformanceUseCase(get()) }
    factory { CompleteOnboardingUseCase(get(), get(), get()) }
}
