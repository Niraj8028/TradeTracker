package com.wallstreet.presentation.strategy.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wallstreet.presentation.strategy.ActionState

@Composable
fun AddStrategyDialog(
    actionState: ActionState,
    onDismiss: () -> Unit,
    onAddClick: (String, String) -> Unit
    ) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }


    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { onAddClick(name, description) },
                enabled = actionState !is ActionState.Loading
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Add Strategy") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Strategy Name") },
                    singleLine = true
                )
                when (actionState) {
                    is ActionState.Loading -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        CircularProgressIndicator()
                    }

                    is ActionState.ValidationError -> {
                        Text(
                            text = actionState.message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    is ActionState.Error -> {
                        Text(
                            text = actionState.message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    is ActionState.Success -> {
                        // Auto close handled outside
                    }

                    else -> Unit
                }
            }
        }
    )

    LaunchedEffect(actionState) {
        if (actionState is ActionState.Success) {
            onDismiss()
        }
    }
}