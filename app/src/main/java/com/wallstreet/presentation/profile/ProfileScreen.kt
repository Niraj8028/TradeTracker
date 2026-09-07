package com.wallstreet.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.wallstreet.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.PrivacyTip
import com.wallstreet.core.preferences.CurrencyPreferences
import com.wallstreet.core.preferences.ThemePreferences
import com.wallstreet.core.preferences.ThemeTypes
import com.wallstreet.core.preferences.currencySymbols
import com.wallstreet.core.util.HapticStyle
import com.wallstreet.core.util.haptic
import com.wallstreet.core.util.hapticClickable
import com.wallstreet.presentation.profile.components.ConfirmationDialog
import com.wallstreet.presentation.profile.components.ThemeToggle
import com.wallstreet.ui.theme.DangerRed
import com.wallstreet.ui.theme.LocalCurrencySymbol
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
    val view = LocalView.current
    val themePrefs = remember { ThemePreferences(context) }
    val currencyPrefs = remember { CurrencyPreferences(context) }
    val currentCurrencyCode by currencyPrefs.currencyCode.collectAsState(initial = "USD")
    var showCurrencySheet by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isLoggedOut) {
        if (isLoggedOut) onLogout()
    }

    if (user == null) {
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Text("No user logged in", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            // Opaque root so tab-switch slide transitions don't bleed through.
            .background(MaterialTheme.colorScheme.background)
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
                ThemeToggle()
            }
        }

        // ── Preferences ──────────────────────────────────────────────
        ProfileSection(label = "PREFERENCES") {
            ProfileMenuItem(
                icon = Icons.Filled.Language,
                label = "Currency",
                sublabel = "${LocalCurrencySymbol.current} · $currentCurrencyCode",
                onClick = { showCurrencySheet = true }
            )
        }

        // ── Account ──────────────────────────────────────────────────
        ProfileSection(label = "ACCOUNT") {
            ProfileMenuItem(
                icon = Icons.Filled.AdminPanelSettings,
                label = "Security & Privacy",
                onClick = onSecurityPrivacy
            )
            HorizontalDivider(
                modifier = Modifier.padding(start = 56.dp),
                color = MaterialTheme.colorScheme.outline.copy(0.4f),
                thickness = 0.5.dp
            )
            ProfileMenuItem(
                icon = Icons.Filled.PrivacyTip,
                label = "Privacy Policy",
                onClick = onPrivacyPolicy
            )
            HorizontalDivider(
                modifier = Modifier.padding(start = 56.dp),
                color = MaterialTheme.colorScheme.outline.copy(0.4f),
                thickness = 0.5.dp
            )
            ProfileMenuItem(
                icon = Icons.Filled.Gavel,
                label = "Terms of Service",
                onClick = onTermsOfService
            )
        }

        // ── Danger zone ──────────────────────────────────────────────
        ProfileSection(label = "DANGER ZONE") {
            ProfileMenuItem(
                icon = Icons.Filled.PersonRemove,
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
            onClick = { view.haptic(HapticStyle.Medium); showLogoutDialog = true },
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
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Logout", fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(8.dp))
    }

    if (showCurrencySheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showCurrencySheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Text(
                    text = "Select Currency",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                currencySymbols.entries.forEachIndexed { index, (code, symbol) ->
                    val isSelected = code == currentCurrencyCode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .hapticClickable(HapticStyle.Light) {
                                viewModel.setCurrency(code)
                                showCurrencySheet = false
                            }
                            .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$symbol  $code",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    if (index < currencySymbols.size - 1) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }

    ConfirmationDialog(
        show = showLogoutDialog,
        title = "Logout",
        message = "Are you sure you want to logout?",
        confirmText = "Logout",
        isDestructive = true,
        icon = rememberVectorPainter(Icons.AutoMirrored.Filled.Logout),
        onConfirm = {
            view.haptic(HapticStyle.Strong)
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
    icon: ImageVector,
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
            .hapticClickable(HapticStyle.Light, onClick = onClick)
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
                imageVector = icon,
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
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}
