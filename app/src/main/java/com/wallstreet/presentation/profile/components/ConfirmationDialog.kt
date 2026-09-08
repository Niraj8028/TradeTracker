package com.wallstreet.presentation.profile.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.platform.LocalView
import com.wallstreet.core.util.HapticStyle
import com.wallstreet.core.util.haptic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ConfirmationDialog(
    show: Boolean,
    title: String,
    message: String,
    confirmText: String,
    isDestructive: Boolean = false,
    icon: Painter? = null,
 
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!show) return

    val view = LocalView.current
    val actionColor = if (isDestructive)
        MaterialTheme.colorScheme.error
    else
        MaterialTheme.colorScheme.primary

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            if (icon != null) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = actionColor,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        dismissButton = {
            TextButton(onClick = { view.haptic(HapticStyle.Light); onDismiss() }) {
                Text(
                    "Cancel",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { view.haptic(HapticStyle.Strong); onConfirm() }) {
                Text(
                    confirmText,
                    color = actionColor,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold // Confirm action stands out
                )
            }
        }
    )
}