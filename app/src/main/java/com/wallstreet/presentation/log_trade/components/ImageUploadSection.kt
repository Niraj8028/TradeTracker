package com.wallstreet.presentation.log_trade.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wallstreet.ui.theme.DarkSurfaceVariant
import com.wallstreet.ui.theme.DarkTextSecondary
import com.wallstreet.ui.theme.DarkTextTertiary
import com.wallstreet.ui.theme.PrimaryBlue

@Composable
fun ImageUploadSection(
    imageUri: String?,
    onImagePick: () -> Unit
    ) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "TRADE SCREENSHOT (OPTIONAL)",
            style = MaterialTheme.typography.labelMedium,
            color = DarkTextSecondary
        )
        Surface(
            onClick = onImagePick,
            shape = RoundedCornerShape(12.dp),
            color = DarkSurfaceVariant,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if(imageUri != null ) PrimaryBlue else DarkTextTertiary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                if (imageUri != null) {
                    Text(
                        "Image Selected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PrimaryBlue
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Upload Image",
                            tint = DarkTextTertiary,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            "Tap to upload screenshot",
                            style = MaterialTheme.typography.bodySmall,
                            color = DarkTextTertiary
                        )

                    }
                }
            }

        }
    }
}