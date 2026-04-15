package com.wallstreet.presentation.strategy.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wallstreet.presentation.home.TimePeriod
import com.wallstreet.presentation.home.components.PeriodSelector
import com.wallstreet.presentation.strategy.StrategiesUiState

@Composable
fun StrategySuccessView(
    uiState: StrategiesUiState.Success,
    padding: PaddingValues,
    timePeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit,
    onStrategyClick: (String) -> Unit,
    isSelectionMode: Boolean,
    selectedStrategies: SnapshotStateList<String>,
    onSelectionChanged: (String, Boolean) -> Unit,
    ) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp, top = 8.dp)
    ) {
        if (!isSelectionMode) {
            item {
                PeriodSelector(
                    selectedPeriod = timePeriod,
                    onPeriodSelected = onPeriodSelected
                )
            }
        }
        if (uiState.strategyStats.isEmpty()) {
            item { EmptyStrategiesView() }
        } else {
            items(
                items = uiState.strategyStats,
                key = { it.strategy.id }
            ) { strategyStats ->
                val isSelected = selectedStrategies.contains(strategyStats.strategy.id)
                StrategyCard(
                    stats = strategyStats,
                    onStrategyClick = {
                        if(isSelectionMode){
                           onSelectionChanged(
                               strategyStats.strategy.id,
                               !isSelected
                           )
                        } else {
                            onStrategyClick(strategyStats.strategy.id)
                        }
                    },
                    isSelectionMode = isSelectionMode,
                    isSelected = isSelected,
                    onSelectionChanged = {
                        onSelectionChanged(strategyStats.strategy.id, it)
                    }
                )
            }
        }
    }
}


@Composable
private fun EmptyStrategiesView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No strategies yet.\nTap + to add one.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
