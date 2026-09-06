package com.wallstreet.presentation.auth.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

/**
 * "Don't have an account? **Sign up**" style switch link — muted [prefix] followed by an
 * accent-colored [actionText], wrapped in a [TextButton].
 */
@Composable
fun AuthTextLink(
    prefix: String,
    actionText: String,
    onClick: () -> Unit,
) {
    TextButton(onClick = onClick) {
        Text(
            buildAnnotatedString {
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                    append(prefix)
                }
                withStyle(
                    SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                ) {
                    append(actionText)
                }
            }
        )
    }
}
