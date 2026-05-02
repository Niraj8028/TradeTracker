package com.wallstreet.presentation.analytics.components

import androidx.compose.foundation.background
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

data class TabItem(val name: String)


@Composable
fun AppTabRow(tabs: List<TabItem>, selectedIndex: Int, onTabChange: (Int) -> Unit) {


    TabRow(
        selectedTabIndex = selectedIndex,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.primary,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                color = MaterialTheme.colorScheme.primary
            )
        },
        divider = {
            HorizontalDivider(
                Modifier,
                DividerDefaults.Thickness,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
        }
    ) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = index == selectedIndex,
                onClick = { onTabChange(index) },
                text = {
                    Text(
                        tab.name,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (index == selectedIndex)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                },
            )
        }
    }

}