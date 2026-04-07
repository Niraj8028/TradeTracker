package com.wallstreet.di

import com.wallstreet.data.repository.AuthRepositoryImpl
import com.wallstreet.data.repository.TradeRepositoryImpl
import com.wallstreet.data.repository.StrategyRepositoryImpl
import com.wallstreet.domain.repository.AuthRepository
import com.wallstreet.domain.repository.TradeRepository
import com.wallstreet.domain.repository.StrategyRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<TradeRepository> { TradeRepositoryImpl(get()) }
    single<StrategyRepository> {
        StrategyRepositoryImpl(
            get(), get()
        )
    }
}