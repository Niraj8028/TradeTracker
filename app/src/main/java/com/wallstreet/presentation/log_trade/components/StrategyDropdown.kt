package com.wallstreet.presentation.log_trade.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wallstreet.ui.theme.DarkSurface
import com.wallstreet.ui.theme.DarkSurfaceVariant
import com.wallstreet.ui.theme.DarkTextPrimary
import com.wallstreet.ui.theme.DarkTextSecondary
import com.wallstreet.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrategyDropdown(
    selectedStrategy: String,
    strategies: List<String>,
    onStrategySelected: (String) -> Unit
    ) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedStrategy,
            onValueChange = {},
            textStyle = MaterialTheme.typography.bodyMedium,
            readOnly = true,
            label = {
                Text(
                    "SELECT STRATEGY",
                    style = MaterialTheme.typography.labelMedium,
                    color = DarkTextSecondary
                )
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkSurfaceVariant,
                unfocusedContainerColor = DarkSurfaceVariant,
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = DarkTextPrimary,
                unfocusedTextColor = DarkTextPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(DarkSurface)
        ) {
            strategies.forEach { strategy ->
                DropdownMenuItem(
                    text = {
                        Text(
                            strategy,
                            color = DarkTextPrimary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    onClick = {
                        onStrategySelected(strategy)
                        expanded = false
                    },
                    colors = MenuDefaults.itemColors(
                        textColor = DarkTextPrimary
                    )
                )
            }
        }
    }
}

