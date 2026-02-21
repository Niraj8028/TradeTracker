package com.wallstreet.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButtonElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.wallstreet.navigation.BottomNavItem
import com.wallstreet.ui.theme.PrimaryBlueDark
import com.wallstreet.ui.theme.White

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
                        contentDescription = stringResource(item.label),
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text(stringResource(item.label)) }
            )
        }

        // FAB in center
        NavigationBarItem(
            selected = false,
            onClick = {
                BottomNavItem.items[2].key
            },
            icon = {
                FloatingActionButton(onClick = onFabClick,
                    modifier = Modifier.size(55.dp),
                    containerColor = PrimaryBlueDark,
                    contentColor = White,
                    elevation = FloatingActionButtonDefaults.elevation(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Log Trade",
                        modifier = Modifier.size(30.dp)
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
                        contentDescription = stringResource(item.label),
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text(stringResource(item.label)) }
            )
        }
    }
}