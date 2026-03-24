package com.wallstreet.di

import com.wallstreet.presentation.auth.login.LoginViewModel
import com.wallstreet.presentation.auth.verification.EmailVerifyViewModel
import com.wallstreet.presentation.auth.register.RegisterViewModel
import com.wallstreet.presentation.profile.ProfileViewModel
import com.wallstreet.presentation.splash.SplashViewModel
import com.wallstreet.presentation.log_trade.LogTradeViewModel
import com.wallstreet.presentation.home.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::SplashViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::LogTradeViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::EmailVerifyViewModel)

}