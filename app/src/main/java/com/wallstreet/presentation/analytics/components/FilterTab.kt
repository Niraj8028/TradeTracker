package com.wallstreet.presentation.analytics.components

import androidx.compose.foundation.layout.Arrangement

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

sealed class FilterOption(val label: String) {
    data object OneWeek : FilterOption("1W")
    data object OneMonth : FilterOption("1M")
    data object ThreeMonths : FilterOption("3M")
    data object SixMonths : FilterOption("6M")
    data object OneYear : FilterOption("1Y")
    data object All : FilterOption("All")
    companion object {
        val all by lazy {
            listOf<FilterOption>(
                OneWeek,
                OneMonth,
                ThreeMonths,
                SixMonths,
                OneYear,
                All
            )
        }

            
    }
}


@Composable
fun FilterTab(
    filters: List<FilterOption>,
    selected: FilterOption,
    onSelectFilter: (FilterOption) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),


        ) {
        items(filters, key = { it.label }) { filter ->


            FilterChip(
                selected = filter == selected,
                onClick = { onSelectFilter(filter) },
                label = { Text(filter.label, style = MaterialTheme.typography.titleMedium) },
                shape = RoundedCornerShape(16.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                ),
            )
        }
    }
}