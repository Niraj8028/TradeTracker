package com.wallstreet.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.wallstreet.R
import com.wallstreet.core.preferences.ThemePreferences
import com.wallstreet.core.preferences.ThemeTypes
import com.wallstreet.presentation.profile.components.ConfirmationDialog
import com.wallstreet.presentation.profile.components.ThemeToggle
import com.wallstreet.ui.theme.DangerRed
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel(),
    onSecurityPrivacy: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    onTermsOfService: () -> Unit,
    onDeleteAccount: () -> Unit,
) {
    val user = viewModel.user
    val isLoggedOut by viewModel.isLoggedOut.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val themePrefs = remember { ThemePreferences(context) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isLoggedOut) {
        if (isLoggedOut) onLogout()
    }

    if (user == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No user logged in", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── Profile header card ──────────────────────────────────────
        val cardShape = RoundedCornerShape(20.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, cardShape, ambientColor = Color.Black.copy(0.2f), spotColor = Color.Black.copy(0.2f))
                .clip(cardShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(0.3f), cardShape)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = user.photoUrl,
                    contentDescription = "Profile photo",
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape),
                    placeholder = painterResource(R.drawable.profile_circle),
                    error = painterResource(R.drawable.profile_circle),
                    colorFilter = if (user.photoUrl == null)
                        ColorFilter.tint(MaterialTheme.colorScheme.primary)
                    else null
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // ── Appearance ───────────────────────────────────────────────
        ProfileSection(label = "APPEARANCE") {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = "App Theme",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(10.dp))
                ThemeToggle()
            }
        }

        // ── Account ──────────────────────────────────────────────────
        ProfileSection(label = "ACCOUNT") {
            ProfileMenuItem(
                icon = painterResource(R.drawable.user_shield),
                label = "Security & Privacy",
                onClick = onSecurityPrivacy
            )
            HorizontalDivider(
                modifier = Modifier.padding(start = 56.dp),
                color = MaterialTheme.colorScheme.outline.copy(0.4f),
                thickness = 0.5.dp
            )
            ProfileMenuItem(
                icon = painterResource(R.drawable.lock),
                label = "Privacy Policy",
                onClick = onPrivacyPolicy
            )
            HorizontalDivider(
                modifier = Modifier.padding(start = 56.dp),
                color = MaterialTheme.colorScheme.outline.copy(0.4f),
                thickness = 0.5.dp
            )
            ProfileMenuItem(
                icon = painterResource(R.drawable.terms),
                label = "Terms of Service",
                onClick = onTermsOfService
            )
        }

        // ── Danger zone ──────────────────────────────────────────────
        ProfileSection(label = "DANGER ZONE") {
            ProfileMenuItem(
                icon = painterResource(R.drawable.delete),
                label = "Delete Account",
                sublabel = "Permanently removes all your data",
                iconTint = MaterialTheme.colorScheme.error,
                iconBgColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                labelColor = MaterialTheme.colorScheme.error,
                onClick = onDeleteAccount
            )
        }

        // ── Logout button ────────────────────────────────────────────
        OutlinedButton(
            onClick = { showLogoutDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.error)
            )
        ) {
            Icon(
                painter = painterResource(R.drawable.log_out),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Logout", fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(8.dp))
    }

    ConfirmationDialog(
        show = showLogoutDialog,
        title = "Logout",
        message = "Are you sure you want to logout?",
        confirmText = "Logout",
        isDestructive = true,
        icon = painterResource(R.drawable.log_out),
        onConfirm = {
            showLogoutDialog = false
            viewModel.onSignOutClicked()
        },
        onDismiss = { showLogoutDialog = false }
    )
}

@Composable
private fun ProfileSection(
    label: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        val shape = RoundedCornerShape(16.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, shape, ambientColor = Color.Black.copy(0.15f), spotColor = Color.Black.copy(0.15f))
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(0.3f), shape),
            content = content
        )
    }
}

@Composable
private fun ProfileMenuItem(
    icon: Painter,
    label: String,
    sublabel: String? = null,
    onClick: () -> Unit,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    iconBgColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
    labelColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = labelColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (sublabel != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = sublabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Icon(
            painter = painterResource(R.drawable.scheveron_arrow),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
    }
}
