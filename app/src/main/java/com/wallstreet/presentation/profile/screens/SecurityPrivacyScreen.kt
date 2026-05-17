package com.wallstreet.presentation.profile.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.wallstreet.R
import com.wallstreet.core.result.Result
import com.wallstreet.domain.model.User
import com.wallstreet.presentation.profile.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityPrivacyScreen(
    viewModel: ProfileViewModel,
    onDeleteInApp: () -> Unit,
    onBack: () -> Unit
) {
    val user = viewModel.user
    val context = LocalContext.current
    val resetEmailState by viewModel.resetEmailState.collectAsState()

    LaunchedEffect(resetEmailState) {
        when (resetEmailState) {
            is Result.Success -> {
                Toast.makeText(
                    context,
                    context.getString(R.string.security_reset_email_sent),
                    Toast.LENGTH_LONG
                ).show()
                viewModel.clearResetEmailState()
            }
            is Result.Error -> {
                Toast.makeText(
                    context,
                    (resetEmailState as Result.Error).message,
                    Toast.LENGTH_LONG
                ).show()
                viewModel.clearResetEmailState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.security_privacy_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Authentication Summary
            SecuritySection(title = stringResource(R.string.security_header_auth)) {
                val provider = if (user?.providerId == "google.com") {
                    stringResource(R.string.security_auth_google)
                } else {
                    stringResource(R.string.security_auth_email)
                }
                
                SecurityItem(
                    icon = Icons.Default.Person,
                    title = stringResource(R.string.security_signed_in_as, provider),
                    subtitle = user?.email ?: ""
                )
            }

            // 2. Password Management (Only for Email users)
            if (user?.providerId != "google.com") {
                SecuritySection(title = stringResource(R.string.security_header_password)) {
                    SecurityItem(
                        icon = Icons.Default.Lock,
                        title = stringResource(R.string.security_send_reset_email),
                        subtitle = stringResource(R.string.login_reset_password_message),
                        onClick = { viewModel.sendPasswordResetEmail() },
                        showChevron = true,
                        isLoading = resetEmailState is Result.Loading
                    )
                }
            }

            // 3. Danger Zone
            SecuritySection(title = stringResource(R.string.security_header_danger)) {
                // Option 1: In-App (The one from your screenshot)
                SecurityItem(
                    icon = Icons.Default.Delete,
                    title = "Delete Account",
                    subtitle = "Permanently delete your account and data immediately",
                    titleColor = MaterialTheme.colorScheme.error,
                    onClick = onDeleteInApp,
                    showChevron = true
                )

                // Option 2: External Portal (Netlify)
                SecurityItem(
                    icon = Icons.Default.Shield,
                    title = stringResource(R.string.security_delete_account_request),
                    subtitle = stringResource(R.string.security_delete_account_request_desc),
                    titleColor = MaterialTheme.colorScheme.error,
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, context.getString(R.string.security_delete_account_url).toUri())
                        context.startActivity(intent)
                    },
                    showChevron = true
                )
            }
        }
    }
}

@Composable
fun SecuritySection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                content()
            }
        }
    }
}

@Composable
fun SecurityItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null,
    showChevron: Boolean = false,
    isLoading: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null && !isLoading) { onClick?.invoke() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (titleColor == MaterialTheme.colorScheme.error) titleColor else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = titleColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        } else if (showChevron) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}