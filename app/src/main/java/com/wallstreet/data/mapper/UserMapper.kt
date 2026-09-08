package com.wallstreet.data.mapper

import com.wallstreet.data.model.UserDto
import com.wallstreet.domain.model.User
import com.wallstreet.domain.model.UserSettings

fun UserDto.toDomain(): User {
    return User(
        id = id,
        name = name,
        email = email,
        photoUrl = photoUrl,
//        createdAt = createdAt
    )
}

fun User.toDto(): UserDto {
    return UserDto(
        id = id,
        name = name,
        email = email,
        photoUrl = photoUrl,
//        createdAt = createdAt,
//        settings = TODO(),
//        settings = UserSettings(
//            isDarkMode = settings.isDarkMode,
//            baseCurrency = settings.baseCurrency,
//            notificationsEnabled = settings.notificationsEnabled
//        )
    )
}