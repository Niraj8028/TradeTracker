package com.wallstreet.presentation.onboarding.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.wallstreet.ui.theme.BorderColors

@Composable
fun RoleCard(role: String, imageRes: Int, isSelected: Boolean, onClick: () -> Unit) {
    val borderColor =
        if (isSelected)
            BorderColors.current.primary
        else
            BorderColors.current.secondary

    Box(
        modifier = Modifier
            .height(56.dp)

            .clip(RoundedCornerShape(50))
            .clickable { onClick() },

        contentAlignment = Alignment.Center
    ) {

        Image(
            painter = painterResource(imageRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    if (isSelected)
                        Color(0x99FFB300) else
                        Color.Black.copy(alpha = 0.55f)
                )
        )
        Text(
            role,
            style = MaterialTheme.typography.titleLarge,

            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}