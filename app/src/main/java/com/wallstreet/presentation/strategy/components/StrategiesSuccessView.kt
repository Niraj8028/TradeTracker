package com.wallstreet.presentation.strategy.components

import androidx.compose.foundation.background
import com.wallstreet.core.util.HapticStyle
import com.wallstreet.core.util.hapticClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wallstreet.domain.model.Strategy
import com.wallstreet.domain.model.TimePeriod
import com.wallstreet.presentation.home.components.PeriodSelector
import com.wallstreet.presentation.strategy.StrategiesUiState
import com.wallstreet.presentation.strategy.SortDirection
import com.wallstreet.presentation.strategy.StrategySortOption
import com.wallstreet.ui.theme.PrimaryBlue

@Composable
fun StrategySuccessView(
    uiState: StrategiesUiState.Success,
    timePeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit,
    sortOption: StrategySortOption,
    onSortSelected: (StrategySortOption) -> Unit,
    sortDirection: SortDirection,
    onSortDirectionToggled: () -> Unit,
    onStrategyClick: (String) -> Unit,
    isSelectionMode: Boolean,
    selectedStrategies: SnapshotStateList<Strategy>,
    onSelectionChanged: (Strategy, Boolean) -> Unit,
    query: String,
) {
    val allStats = uiState.strategyStats
    val stats = if (query.isBlank()) allStats
    else allStats.filter { it.strategy.name.contains(query, ignoreCase = true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp, top = 4.dp)
    ) {
        item {
            PeriodSelector(selectedPeriod = timePeriod, onPeriodSelected = onPeriodSelected)
        }

        item {
            SortFilterBar(
                count = stats.size,
                sortOption = sortOption,
                onSortSelected = onSortSelected,
                sortDirection = sortDirection,
                onDirectionToggled = onSortDirectionToggled
            )
        }

        if (stats.isEmpty()) {
            item { EmptyStrategiesView(isSearch = query.isNotBlank()) }
        } else {
            itemsIndexed(
                items = stats,
                key = { _, s -> s.strategy.id }
            ) { index, strategyStats ->
                val isSelected = selectedStrategies.contains(strategyStats.strategy)
                val sid = strategyStats.strategy.id
                StrategyCard(
                    stats = strategyStats,
                    rank = index + 1,
                    onStrategyClick = { onStrategyClick(sid) },
                    isSelectionMode = isSelectionMode,
                    isSelected = isSelected,
                    onSelectionChanged = { onSelectionChanged(strategyStats.strategy, it) }
                )
            }
        }
    }
}

@Composable
private fun SortFilterBar(
    count: Int,
    sortOption: StrategySortOption,
    onSortSelected: (StrategySortOption) -> Unit,
    sortDirection: SortDirection,
    onDirectionToggled: () -> Unit
) {
    var sortMenuOpen by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)) {
                    append("$count")
                }
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                    append(" strategies")
                }
            },
            fontSize = 12.5.sp
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Sort dropdown button
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .hapticClickable(HapticStyle.Light) { sortMenuOpen = true }
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = "Sort: ",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = sortOption.label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                DropdownMenu(
                    expanded = sortMenuOpen,
                    onDismissRequest = { sortMenuOpen = false }
                ) {
                    StrategySortOption.entries.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option.label,
                                    fontWeight = if (option == sortOption) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (option == sortOption) PrimaryBlue else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = { onSortSelected(option); sortMenuOpen = false },
                            trailingIcon = if (option == sortOption) {
                                { Icon(Icons.Default.Check, null, tint = PrimaryBlue, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }
            }

            // Direction toggle
            IconButton(
                onClick = onDirectionToggled,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (sortDirection == SortDirection.DESC) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = "Toggle sort direction",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyStrategiesView(isSearch: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isSearch) "No strategies match your search." else "No strategies yet.\nTap + to add one.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
