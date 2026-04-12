import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
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
import coil.size.Scale
import com.wallstreet.presentation.strategy.StrategiesUiState
import com.wallstreet.presentation.strategy.StrategyViewModel
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrategiesScreen(
//    onAddStrategy: () -> Unit,
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
                        onClick = {}
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
            is StrategiesUiState.Error -> StrategyErrorView()
            StrategiesUiState.Loading -> CircularProgressIndicator()
            is StrategiesUiState.Success -> StrategySuccessView()
        }
    }
}

@Composable
fun StrategyErrorView() {
    Text("Error Not yet implemented")
}

@Composable
fun StrategySuccessView() {
    Text("Success Not yet implemented")
}