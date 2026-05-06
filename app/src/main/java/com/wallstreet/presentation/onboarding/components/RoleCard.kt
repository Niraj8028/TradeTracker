package com.wallstreet.presentation.onboarding.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.wallstreet.ui.theme.LocalBorderColors

@Composable
fun RoleCard(role: String, isSelected: Boolean, onClick: () -> Unit) {
    val borderColor =
        if (isSelected)
            LocalBorderColors.current.primary
        else
            LocalBorderColors.current.secondary

    Box() {}
}