package com.wallstreet.presentation.analytics.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.platform.LocalView
import com.wallstreet.core.util.HapticStyle
import com.wallstreet.core.util.haptic
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
import com.wallstreet.domain.model.TimePeriod

sealed class FilterOption(val label: String) {
    data object OneWeek : FilterOption("1W")
    data object OneMonth : FilterOption("1M")
    data object ThreeMonths : FilterOption("3M")
    data object SixMonths : FilterOption("6M")
    data object OneYear : FilterOption("1Y")
    data object All : FilterOption("All")

    fun toTimePeriod(): TimePeriod = when (this) {
        OneWeek -> TimePeriod.ONE_WEEK
        OneMonth -> TimePeriod.ONE_MONTH
        ThreeMonths -> TimePeriod.THREE_MONTHS
        SixMonths -> TimePeriod.SIX_MONTHS
        OneYear -> TimePeriod.ONE_YEAR
        All -> TimePeriod.ALL
    }

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

        fun fromTimePeriod(period: TimePeriod): FilterOption = when (period) {
            TimePeriod.ONE_WEEK -> OneWeek
            TimePeriod.ONE_MONTH -> OneMonth
            TimePeriod.THREE_MONTHS -> ThreeMonths
            TimePeriod.SIX_MONTHS -> SixMonths
            TimePeriod.ONE_YEAR -> OneYear
            TimePeriod.ALL -> All
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
    val view = LocalView.current
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(filters, key = { it.label }) { filter ->
            FilterChip(
                selected = filter == selected,
                onClick = { view.haptic(HapticStyle.Light); onSelectFilter(filter) },
                label = { Text(filter.label, style = MaterialTheme.typography.labelMedium) },
                shape = RoundedCornerShape(16.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = filter == selected,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    selectedBorderColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}
