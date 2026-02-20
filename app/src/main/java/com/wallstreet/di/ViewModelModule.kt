package com.wallstreet.di

import com.wallstreet.presentation.auth.login.LoginViewModel
import com.wallstreet.presentation.auth.register.RegisterViewModel
import com.wallstreet.presentation.profile.ProfileViewModel
import com.wallstreet.presentation.splash.SplashViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::SplashViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::ProfileViewModel)
}