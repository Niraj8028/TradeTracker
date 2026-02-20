package com.wallstreet.di

import com.wallstreet.presentation.auth.login.LoginViewModel
import com.wallstreet.presentation.auth.register.RegisterViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import kotlin.math.sin


val viewModelModule  = module {
    viewModel { LoginViewModel() }
    viewModel { RegisterViewModel() }

}