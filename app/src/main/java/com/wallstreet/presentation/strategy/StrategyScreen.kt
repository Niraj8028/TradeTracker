package com.wallstreet.presentation.strategy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wallstreet.domain.model.Strategy
import com.wallstreet.presentation.home.LoadingView
import com.wallstreet.presentation.strategy.components.AddStrategyDialog
import com.wallstreet.presentation.strategy.components.DeleteStrategyBottomSheet
import com.wallstreet.presentation.strategy.components.EditStrategyDialog
import com.wallstreet.presentation.strategy.components.StrategyErrorView
import com.wallstreet.presentation.strategy.components.StrategySuccessView
import com.wallstreet.ui.theme.PrimaryBlue
import org.koin.androidx.compose.koinViewModel

@Composable
fun StrategiesScreen(
    onStrategyClick: (StrategyId: String) -> Unit,
    onAddStrategy: () -> Unit,
    viewModel: StrategyViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val sortDirection by viewModel.sortDirection.collectAsState()
    val actionState by viewModel.actionState.collectAsState()

    var showAddStrategyDialog by remember { mutableStateOf(false) }
    var showDeleteStrategyConfirmDialog by remember { mutableStateOf(false) }
    var showEditStrategyDialog by remember { mutableStateOf(false) }
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedStrategies = remember { mutableStateListOf<Strategy>() }
    var searchOpen by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    fun exitSearch() { searchOpen = false; query = "" }
    fun exitSelection() { isSelectionMode = false; selectedStrategies.clear() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Compact header ───────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(start = 16.dp, end = 4.dp, top = 6.dp, bottom = 10.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                when {
                    searchOpen -> BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onBackground
                        ),
                        cursorBrush = SolidColor(PrimaryBlue),
                        decorationBox = { innerTextField ->
                            if (query.isEmpty()) {
                                Text(
                                    "Search strategies…",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            innerTextField()
                        }
                    )
                    isSelectionMode -> Text(
                        text = "${selectedStrategies.size} selected",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = (-0.5).sp
                    )
                    else -> Text(
                        text = "Strategies",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = (-0.5).sp
                    )
                }
            }

            when {
                isSelectionMode -> {
                    if (selectedStrategies.size == 1) {
                        IconButton(onClick = {
                            viewModel.clearActionState()
                            showEditStrategyDialog = true
                        }) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Rename",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    if (selectedStrategies.isNotEmpty()) {
                        IconButton(onClick = { showDeleteStrategyConfirmDialog = true }) {
                            Icon(
                                Icons.Default.DeleteForever,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    IconButton(onClick = { exitSelection() }) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                }
                searchOpen -> {
                    IconButton(onClick = { exitSearch() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close search")
                    }
                }
                else -> {
                    IconButton(onClick = { exitSelection(); searchOpen = true }) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = {
                        viewModel.clearActionState()
                        showAddStrategyDialog = true
                    }) {
                        Icon(
                            Icons.Default.AddCircle,
                            contentDescription = "Add Strategy",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = { exitSearch(); isSelectionMode = true }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }

        // ── Content ──────────────────────────────────────────────────────
        when (uiState) {
            is StrategiesUiState.Error -> StrategyErrorView(
                message = (uiState as StrategiesUiState.Error).message,
                padding = PaddingValues(0.dp)
            )
            StrategiesUiState.Loading -> LoadingView()
            is StrategiesUiState.Success -> StrategySuccessView(
                uiState = uiState as StrategiesUiState.Success,
                timePeriod = selectedPeriod,
                onPeriodSelected = viewModel::onPeriodSelected,
                sortOption = sortOption,
                onSortSelected = viewModel::onSortSelected,
                sortDirection = sortDirection,
                onSortDirectionToggled = viewModel::onSortDirectionToggled,
                onStrategyClick = onStrategyClick,
                isSelectionMode = isSelectionMode,
                selectedStrategies = selectedStrategies,
                onSelectionChanged = { strategy, isSelected ->
                    if (isSelected) selectedStrategies.add(strategy)
                    else selectedStrategies.remove(strategy)
                },
                query = query
            )
        }
    }

    if (showAddStrategyDialog) {
        AddStrategyDialog(
            actionState = actionState,
            onDismiss = {
                showAddStrategyDialog = false
                viewModel.clearActionState()
            },
            onAddClick = { name, description ->
                viewModel.addStrategy(name, description)
            }
        )
    }

    if (showDeleteStrategyConfirmDialog) {
        DeleteStrategyBottomSheet(
            selectedCount = selectedStrategies.size,
            actionState = actionState,
            onConfirm = { viewModel.deleteStrategies(selectedStrategies) },
            onDismiss = {
                showDeleteStrategyConfirmDialog = false
                if (actionState is ActionState.Success) exitSelection()
                viewModel.clearActionState()
            }
        )
    }

    if (showEditStrategyDialog && selectedStrategies.size == 1) {
        val strategy = selectedStrategies.first()
        EditStrategyDialog(
            currentName = strategy.name,
            actionState = actionState,
            onDismiss = {
                showEditStrategyDialog = false
                if (actionState is ActionState.Success) exitSelection()
                viewModel.clearActionState()
            },
            onSaveClick = { newName ->
                viewModel.updateStrategy(strategy.copy(name = newName))
            }
        )
    }
}
