package com.wallstreet.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val photoUrl: String? = null,
    val providerId: String? = null
)

data class UserSettings(
    val isDarkMode: Boolean = false,
    val baseCurrency: String = "USD",
    val notificationsEnabled: Boolean = true
)