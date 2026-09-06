package com.wallstreet.core.constants

import com.wallstreet.R
import com.wallstreet.presentation.onboarding.components.UserRole

object AppConstants {
    const val COLLECTION_USERS = "users"
    val mistakes = listOf(
        "FOMO",
        "Early Exit",
        "Large Size",
        "Revenge Trade",
        "No Stop Loss",
        "No Setup",
        "SL Trailed",
        "Small SL",
    )


    val roles: List<UserRole> = listOf(
        UserRole(
            role = "Forex",
            imageRes = R.drawable.onboarding_forex
        ),
        UserRole(
            role = "Options",
            imageRes = R.drawable.onboarding_options
        ),
        UserRole(
            role = "Crypto",
            imageRes = R.drawable.onboarding_crypto
        ),
        UserRole(
            role = "Stocks",
            imageRes = R.drawable.onboarding_stocks
        ),
        UserRole(
            role = "Futures",
            imageRes = R.drawable.onboarding_futures
        ),
        UserRole(
            role = "Swing",
            imageRes = R.drawable.onboarding_swing
        ),
        UserRole(
            role = "Scalping",
            imageRes = R.drawable.onboarding_scalping
        ),
        UserRole(
            role = "Intraday",
            imageRes = R.drawable.onboarding_intraday
        )
    )
}