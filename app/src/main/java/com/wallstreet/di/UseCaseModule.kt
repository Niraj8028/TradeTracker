package com.wallstreet.di

import com.wallstreet.domain.usecase.auth.*
import com.wallstreet.domain.usecase.home.GetHomeStateUsecase
import com.wallstreet.domain.usecase.trade.AddTradeUseCase
import com.wallstreet.domain.usecase.trade.GetTradesUsecase
import org.koin.dsl.module

val useCaseModule = module {
    factory { SignInUseCase(get()) }
    factory { SignInWithGoogleUseCase(get()) }
    factory { SignUpUseCase(get()) }
    factory { SignOutUseCase(get()) }
    factory { GetCurrentUserUseCase(get()) }
    factory { VerifyOtpUseCase(get()) }
    factory { SendPasswordResetEmailUseCase(get()) }
    factory { AddTradeUseCase(get()) }
    factory { GetTradesUsecase(get()) }
    factory { GetHomeStateUsecase(get()) }
}