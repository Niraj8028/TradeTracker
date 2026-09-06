package com.wallstreet.presentation.auth.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wallstreet.R

/** Branded title block shown in an [AuthScaffold] header slot: optional logo, title, subtitle. */
@Composable
fun ColumnScope.AuthHeader(
    title: String,
    subtitle: String,
    showLogo: Boolean = true,
    logoWidthFraction: Float = 0.56f,
) {
    if (showLogo) {
        Image(
            painter = painterResource(R.drawable.globe),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(logoWidthFraction),
        )
        Spacer(Modifier.height(18.dp))
    }
    Text(
        text = title,
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center,
        letterSpacing = (-0.5).sp,
    )
    Spacer(Modifier.height(10.dp))
    Text(
        text = subtitle,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        lineHeight = 20.sp,
        modifier = Modifier.fillMaxWidth(0.9f),
    )
}
