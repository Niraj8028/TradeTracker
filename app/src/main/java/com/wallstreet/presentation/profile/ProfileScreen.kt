package com.wallstreet.presentation.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

data class User(
    val id: String,
    val name: String,
    val email: String
)
@Composable

fun ProfileScreen (user: User) {
    Column(){
        Text("Name: ${user.name}")
        Text("Email: ${user.email}")
    }
}