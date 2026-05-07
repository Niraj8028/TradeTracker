package com.wallstreet.presentation.onboarding.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wallstreet.R


data class UserRole(val role: String, val description: String, val imageRes: Int)

@Composable
fun PageOne(selectedRoles: List<String>, onUserTypeSelected: (String) -> Unit) {


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
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            "Tell us about you",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            "Personalize your trading journal experience",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Normal
            ), color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(25.dp)
        ) {
            items(roles) { role ->
                RoleCard(
                    role = role.role,
                    imageRes = role.imageRes,
                    isSelected = selectedRoles.contains(role.role),
                    onClick = {
                        onUserTypeSelected(role.role)
                    }
                )
            }
        }

    }
}