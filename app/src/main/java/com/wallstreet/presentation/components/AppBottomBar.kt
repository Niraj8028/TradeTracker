package com.wallstreet.presentation.components


import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.wallstreet.navigation.BottomNavItem
import com.wallstreet.navigation.LogTradeKey

@Composable
fun AppBottomBar(
    currentKey: NavKey,
    onItemClick: (NavKey) -> Unit,
    onFabClick: () -> Unit
) {
    NavigationBar {
        val items = BottomNavItem.items

        // Journal + Analytics (left of FAB)
        items.take(2).forEach { item ->
            NavigationBarItem(
                selected = currentKey == item.key,
                onClick = { onItemClick(item.key) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text(item.label) }
            )
        }

        // FAB in center
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = {
                FloatingActionButton(onClick = onFabClick) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Log Trade"
                    )
                }
            },
            label = { Text("") }
        )

        // Strategies + Profile (right of FAB)
        items.drop(2).forEach { item ->
            NavigationBarItem(
                selected = currentKey == item.key,
                onClick = { onItemClick(item.key) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text(item.label) }
            )
        }
    }
}