package com.wallstreet.di

import com.wallstreet.data.repository.AuthRepositoryImpl
import com.wallstreet.domain.repository.AuthRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
 }