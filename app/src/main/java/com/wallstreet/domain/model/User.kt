package com.wallstreet.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val photoUrl: String? = null,
//    val createdAt: Long,
    // TODO work on userSettings
    //    val settings: UserSettings
)

data class UserSettings (
    val isDarkMode: Boolean = false,
    val baseCurrency: String = "USD",
    val notificationsEnabled: Boolean = true
)