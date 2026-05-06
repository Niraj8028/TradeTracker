package com.wallstreet.presentation.onboarding.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp


data class UserRole(val role: String, val description: String)

@Composable
fun PageOne(selected: String?, onUserTypeSelected: (String) -> Unit) {


    val roles: List<UserRole> = listOf(
        UserRole(
            role = "Student",
            description = "Academic"
        ),
        UserRole(
            role = "Professional",
            description = "9-to-5"
        ),
        UserRole(
            role = "Freelancer",
            description = "Self-employed"
        ),
        UserRole(
            role = "Trader",
            description = "Full-time"
        ),
        UserRole(
            role = "Developer",
            description = "Tech"
        ),
        UserRole(
            role = "Designer",
            description = "Creative"
        ),
        UserRole(
            role = "Creator",
            description = "Content"
        ),
        UserRole(
            role = "Other",
            description = "..."
        )
    )
    Column() {

        Text("Tell us about you")
        Text("Personalize your trading journal experience")


        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(roles) { role ->
                RoleCard(
                    role = role.role,
                    isSelected = selected == role.role,
                    onClick = {
                        onUserTypeSelected(role.role)
                    }
                )
            }
        }

    }
}