package com.wallstreet.core.splash


object SplashGate {

    @Volatile
    var isReady: Boolean = false

    @Volatile
    var startDestination: StartDestination = StartDestination.Unknown
}

