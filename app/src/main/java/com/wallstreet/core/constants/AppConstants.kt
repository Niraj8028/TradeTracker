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
            description = "Currencies",
            imageRes = R.drawable.onboarding_forex
        ),
        UserRole(
            role = "Options",
            description = "Contracts",
            imageRes = R.drawable.onboarding_options
        ),
        UserRole(
            role = "Crypto",
            description = "Digital",
            imageRes = R.drawable.onboarding_crypto
        ),
        UserRole(
            role = "Stocks",
            description = "Equity",
            imageRes = R.drawable.onboarding_stocks
        ),
        UserRole(
            role = "Futures",
            description = "Derivatives",
            imageRes = R.drawable.onboarding_futures
        ),
        UserRole(
            role = "Swing",
            description = "Mid-term",
            imageRes = R.drawable.onboarding_swing
        ),
        UserRole(
            role = "Scalping",
            description = "Quick trades",
            imageRes = R.drawable.onboarding_scalping
        ),
        UserRole(
            role = "Intraday",
            description = "Same-day",
            imageRes = R.drawable.onboarding_intraday
        )
    )
}