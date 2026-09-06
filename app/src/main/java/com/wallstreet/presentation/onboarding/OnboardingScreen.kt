package com.wallstreet.presentation.onboarding

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wallstreet.R
import com.wallstreet.core.constants.AppConstants
import com.wallstreet.core.preferences.currencyNames
import com.wallstreet.core.preferences.currencySymbols
import com.wallstreet.presentation.onboarding.components.CurrencyOption
import com.wallstreet.presentation.onboarding.components.OnboardingScaffold
import com.wallstreet.presentation.onboarding.components.RoleTile
import org.koin.androidx.compose.koinViewModel

private enum class Step { Roles, Currency, Notifications }

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val showNotifications = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    val steps = remember(showNotifications) {
        buildList {
            add(Step.Roles)
            add(Step.Currency)
            if (showNotifications) add(Step.Notifications)
        }
    }
    val lastIndex = steps.lastIndex
    val index = uiState.stepIndex.coerceIn(0, lastIndex)

    LaunchedEffect(uiState.finished) {
        if (uiState.finished) onFinish()
    }

    val context = LocalContext.current
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    BackHandler(enabled = index > 0) { viewModel.prevStep() }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onNotificationsResult(granted)
        viewModel.finish()
    }

    val advance: () -> Unit = {
        if (index == lastIndex) viewModel.finish() else viewModel.nextStep(lastIndex)
    }

    AnimatedContent(
        targetState = index,
        transitionSpec = {
            val dir = if (targetState >= initialState) 1 else -1
            slideInHorizontally { w -> dir * w } togetherWith
                slideOutHorizontally { w -> -dir * w }
        },
        label = "onboardingStep",
    ) { animatedIndex ->
        when (steps[animatedIndex.coerceIn(0, lastIndex)]) {
            Step.Roles -> RolesStep(
                stepIndex = animatedIndex,
                stepCount = steps.size,
                selectedRoles = uiState.selectedRoles,
                onToggle = viewModel::onRoleToggled,
                onContinue = advance,
            )

            Step.Currency -> CurrencyStep(
                stepIndex = animatedIndex,
                stepCount = steps.size,
                isLast = index == lastIndex,
                loading = uiState.isFinishing,
                selected = uiState.selectedCurrency,
                onSelect = viewModel::onCurrencySelected,
                onBack = viewModel::prevStep,
                onContinue = advance,
            )

            Step.Notifications -> NotificationsStep(
                stepIndex = animatedIndex,
                stepCount = steps.size,
                loading = uiState.isFinishing,
                onBack = viewModel::prevStep,
                onEnable = {
                    notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                },
                onSkip = {
                    viewModel.onNotificationsResult(false)
                    viewModel.finish()
                },
            )
        }
    }
}

@Composable
private fun RolesStep(
    stepIndex: Int,
    stepCount: Int,
    selectedRoles: List<String>,
    onToggle: (String) -> Unit,
    onContinue: () -> Unit,
) {
    OnboardingScaffold(
        stepIndex = stepIndex,
        stepCount = stepCount,
        title = stringResource(R.string.onboarding_roles_title),
        subtitle = stringResource(R.string.onboarding_roles_subtitle),
        primaryText = stringResource(R.string.onboarding_continue),
        primaryEnabled = selectedRoles.isNotEmpty(),
        onPrimary = onContinue,
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValuesBottom,
        ) {
            items(AppConstants.roles) { role ->
                RoleTile(
                    role = role.role,
                    imageRes = role.imageRes,
                    isSelected = selectedRoles.contains(role.role),
                    onClick = { onToggle(role.role) },
                )
            }
        }
    }
}

@Composable
private fun CurrencyStep(
    stepIndex: Int,
    stepCount: Int,
    isLast: Boolean,
    loading: Boolean,
    selected: String,
    onSelect: (String) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
) {
    OnboardingScaffold(
        stepIndex = stepIndex,
        stepCount = stepCount,
        title = stringResource(R.string.onboarding_currency_title),
        subtitle = stringResource(R.string.onboarding_currency_subtitle),
        primaryText = stringResource(
            if (isLast) R.string.onboarding_finish else R.string.onboarding_continue
        ),
        primaryLoading = loading,
        onPrimary = onContinue,
        onBack = onBack,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValuesBottom,
        ) {
            items(currencySymbols.keys.toList()) { code ->
                CurrencyOption(
                    code = code,
                    symbol = currencySymbols[code] ?: "$",
                    name = currencyNames[code] ?: code,
                    isSelected = selected == code,
                    onClick = { onSelect(code) },
                )
            }
        }
    }
}

@Composable
private fun NotificationsStep(
    stepIndex: Int,
    stepCount: Int,
    loading: Boolean,
    onBack: () -> Unit,
    onEnable: () -> Unit,
    onSkip: () -> Unit,
) {
    OnboardingScaffold(
        stepIndex = stepIndex,
        stepCount = stepCount,
        title = stringResource(R.string.onboarding_notifications_title),
        subtitle = stringResource(R.string.onboarding_notifications_subtitle),
        primaryText = stringResource(R.string.onboarding_enable_notifications),
        primaryLoading = loading,
        onPrimary = onEnable,
        onBack = onBack,
        secondaryText = stringResource(R.string.onboarding_maybe_later),
        onSecondary = onSkip,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(8.dp))

            // Layered hero icon
            Box(contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .size(168.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.06f))
                )
                Box(
                    Modifier
                        .size(116.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                )
                Icon(
                    Icons.Filled.NotificationsActive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(52.dp),
                )
            }

            Spacer(Modifier.height(28.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                NotifBenefitRow(
                    icon = Icons.Filled.EditNote,
                    title = "Journaling reminders",
                    detail = "A gentle nudge to log the day's trades",
                )
                NotifBenefitRow(
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    title = "Weekly review",
                    detail = "Your P&L and mistakes, summed up each week",
                )
                NotifBenefitRow(
                    icon = Icons.Filled.LocalFireDepartment,
                    title = "Streak alerts",
                    detail = "Keep your discipline streak alive",
                )
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun NotifBenefitRow(
    icon: ImageVector,
    title: String,
    detail: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private val PaddingValuesBottom =
    androidx.compose.foundation.layout.PaddingValues(bottom = 12.dp)
