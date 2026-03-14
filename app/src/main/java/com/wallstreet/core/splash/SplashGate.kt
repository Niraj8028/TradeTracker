package com.wallstreet.core.splash


import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object SplashGate {
    private val _startDestination = MutableStateFlow<StartDestination?>(null)
    val startDestination = _startDestination.asStateFlow()

    fun resolve(destination: StartDestination) {
        _startDestination.value = destination
    }
}