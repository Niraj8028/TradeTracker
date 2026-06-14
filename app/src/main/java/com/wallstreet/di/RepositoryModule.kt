package com.wallstreet.di

import com.wallstreet.data.repository.AnalyticsRepositoryImp
import com.wallstreet.data.repository.AuthRepositoryImpl
import com.wallstreet.data.repository.TradeRepositoryImpl
import com.wallstreet.data.repository.StrategyRepositoryImpl
import com.wallstreet.data.repository.UserRepositoryImpl
import com.wallstreet.domain.repository.AnalyticsRepository
import com.wallstreet.domain.repository.AuthRepository
import com.wallstreet.domain.repository.TradeRepository
import com.wallstreet.domain.repository.StrategyRepository
import com.wallstreet.domain.repository.UserRepository
import org.koin.dsl.module

val repositoryModule = module {

    single<AuthRepository> { AuthRepositoryImpl(
        get(), get(), get(), get(), get()
    ) }
    single<TradeRepository> { TradeRepositoryImpl(get(), get(), get()) }
    single<StrategyRepository> {
        StrategyRepositoryImpl(
            get(), get()
        )
    }
    single<AnalyticsRepository> { AnalyticsRepositoryImp() }
    single<UserRepository> { UserRepositoryImpl(get()) }
}
