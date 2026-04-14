import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.wallstreet.presentation.home.LoadingView
import com.wallstreet.presentation.strategy.StrategiesUiState
import com.wallstreet.presentation.strategy.StrategyViewModel
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Strategies",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    ) },
                actions = {
                    IconButton(
                        onClick = onAddStrategy
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Strategy",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
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
                onStrategyClick = onStrategyClick
            )
        }
    }
}


