package com.wallstreet.data.model

import com.google.firebase.firestore.DocumentId
import com.wallstreet.domain.model.UserSettings

data class UserDto(
    @DocumentId
    val id: String,
    val name: String,
    val email: String,
    val photoUrl: String? = null,
//    val createdAt: Long,
//    val settings: UserSettings
)
