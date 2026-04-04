package com.wallstreet.presentation.strategy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wallstreet.domain.model.Strategy
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrategyScreen(
    viewModel: StrategyViewModel = koinViewModel()
) {
    val strategies by viewModel.strategies.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    var showDialog by remember { mutableStateOf(false) }
    var editStrategy by remember { mutableStateOf<Strategy?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    // 🔔 Error handling
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Strategies") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editStrategy = null
                showDialog = true
            }) {
                Text("+")
            }
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                strategies.isEmpty() -> {
                    Text(
                        text = "No strategies yet",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn {
                        items(
                            items = strategies,
                            key = { it.id }
                        ) { strategy ->
                            StrategyItem(
                                strategy = strategy,
                                onDelete = { viewModel.deleteStrategy(strategy) },
                                onEdit = {
                                    editStrategy = strategy
                                    showDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // 🔥 Add / Edit Dialog (single source)
    if (showDialog) {
        StrategyDialog(
            initialStrategy = editStrategy,
            onDismiss = { showDialog = false },
            onConfirm = { name, description ->

                if (editStrategy == null) {
                    // ➕ ADD
                    viewModel.addStrategy(
                        Strategy(
                            id = "",
                            name = name,
                            description = description
                        )
                    )
                } else {
                    // ✏️ UPDATE
                    viewModel.updateStrategy(
                        editStrategy!!.copy(
                            name = name,
                            description = description
                        )
                    )
                }

                showDialog = false
            }
        )
    }
}

@Composable
fun StrategyDialog(
    initialStrategy: Strategy? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialStrategy?.name ?: "") }
    var description by remember { mutableStateOf(initialStrategy?.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialStrategy == null) "Add Strategy" else "Edit Strategy"
            )
        },
        text = {
            Column {

                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Strategy Name") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), description.trim())
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun StrategyItem(
    strategy: Strategy,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = strategy.name,
                style = MaterialTheme.typography.titleMedium
            )

            if (strategy.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = strategy.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row {
                Button(onClick = onEdit) {
                    Text("Edit")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(onClick = onDelete) {
                    Text("Delete")
                }
            }
        }
    }
}