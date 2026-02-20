package com.wallstreet.di

import com.wallstreet.domain.usecase.auth.*
import org.koin.dsl.module

val useCaseModule = module {
    factory { SignInUseCase(get()) }
    factory { SignInWithGoogleUseCase(get()) }
    factory { SignUpUseCase(get()) }
    factory { SignOutUseCase(get()) }
    factory { GetCurrentUserUseCase(get()) }


}