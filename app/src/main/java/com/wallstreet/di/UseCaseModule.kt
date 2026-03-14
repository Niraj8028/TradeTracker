package com.wallstreet.di

import com.wallstreet.domain.usecase.auth.*
import com.wallstreet.domain.usecase.trade.AddTradeUseCase
import org.koin.dsl.module

val useCaseModule = module {
    factory { SignInUseCase(get()) }
    factory { SignInWithGoogleUseCase(get()) }
    factory { SignUpUseCase(get()) }
    factory { SignOutUseCase(get()) }
    factory { GetCurrentUserUseCase(get()) }
    factory { AddTradeUseCase(get()) }
    factory { VerifyOtpUseCase(get()) }
    factory { SendPasswordResetEmailUseCase(get()) }

    factory { AddTradeUseCase(get()) }

}