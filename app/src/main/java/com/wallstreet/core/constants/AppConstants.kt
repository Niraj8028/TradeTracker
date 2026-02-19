package com.wallstreet.core.constants

object AppConstants {
    // Firestore collections
    const val COLLECTION_USERS = "users"
    const val COLLECTION_TRADES = "trades"
    const val COLLECTION_JOURNALS = "journals"
    const val COLLECTION_STRATEGIES = "strategies"

    // Pagination
    const val PAGE_SIZE = 20

    // DataStore
    const val PREFS_NAME = "user_prefs"
    const val KEY_ONBOARDING_SHOWN = "onboarding_shown"
    const val KEY_USER_ID = "user_id"
}