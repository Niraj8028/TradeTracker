import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.wallstreet.presentation.home.LoadingView
import com.wallstreet.presentation.strategy.StrategiesUiState
import com.wallstreet.presentation.strategy.StrategyViewModel
import com.wallstreet.presentation.strategy.components.AddStrategyDialog
import com.wallstreet.presentation.strategy.components.StrategyErrorView
import com.wallstreet.presentation.strategy.components.StrategySuccessView
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrategiesScreen(
    onStrategyClick: (StrategyId: String) -> Unit,
    onAddStrategy: () -> Unit,
    viewModel: StrategyViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    var showAddStrategyDialog by remember { mutableStateOf(false) }
    val actionState by viewModel.actionState.collectAsState()
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedStrategies = remember { mutableStateListOf<String>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text =  if (isSelectionMode)
                            "${selectedStrategies.size} selected"
                        else "Strategies",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    ) },
                actions = {
                    if(!isSelectionMode){
                        IconButton(
                            onClick = { showAddStrategyDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = "Add Strategy",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        IconButton(
                            onClick = { isSelectionMode = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    } else {
                        IconButton(onClick = {
                            isSelectionMode = false
                            selectedStrategies.clear()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel")
                        }
                    }

                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )

            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        when(uiState) {
            is StrategiesUiState.Error -> StrategyErrorView(
                message = (uiState as StrategiesUiState.Error).message,
                padding = padding
            )
            StrategiesUiState.Loading -> LoadingView()
            is StrategiesUiState.Success -> StrategySuccessView(
                uiState = uiState as StrategiesUiState.Success,
                padding = padding,
                timePeriod = selectedPeriod,
                onPeriodSelected = viewModel::onPeriodSelected,
                onStrategyClick = onStrategyClick,
                isSelectionMode = isSelectionMode,
                selectedStrategies = selectedStrategies,
                onSelectionChanged = { id, isSelected ->
                    if (isSelected) {
                        selectedStrategies.add(id)
                    } else {
                        selectedStrategies.remove(id)
                    }

                }

            )
        }
    }
    if (showAddStrategyDialog) {
        AddStrategyDialog(
            actionState = actionState,
            onDismiss = {
                showAddStrategyDialog = false
                viewModel.clearActionState()
            },
            onAddClick = { name, description ->
                viewModel.addStrategy(name, description)
            }
        )
    }

}



