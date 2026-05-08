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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wallstreet.R
import com.wallstreet.core.constants.AppConstants


data class UserRole(val role: String, val description: String, val imageRes: Int)

@Composable
fun PageOne(selectedRoles: List<String>, onUserTypeSelected: (String) -> Unit) {


    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = stringResource(R.string.onboarding_page_one_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.onboarding_page_one_subtitle),
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
            items(AppConstants.roles) { role ->
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