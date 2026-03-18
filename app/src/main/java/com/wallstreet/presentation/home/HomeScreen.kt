package com.wallstreet.presentation.home

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wallstreet.domain.model.HomeStats
import org.koin.androidx.compose.koinViewModel


@Composable
fun HomeScreen(viewModel: HomeViewModel = koinViewModel()) {
    val uiState by viewModel.homeUiState.collectAsState()
    val scrollState = rememberScrollState()

    HomeContent(uiState, scrollState)
}

@Composable
fun HomeContent(uiState: HomeUiState, scrollState: ScrollState) {
    when(uiState) {
        is HomeUiState.Error -> {
            Text("ErrorView", style = MaterialTheme.typography.bodyMedium)
        }
        HomeUiState.Loading -> {
            LoadingView()
        }
        is HomeUiState.Success -> {
            SuccessView(uiState.stats, scrollState)
        }
    }
}

@Composable
fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun SuccessView(uiState: HomeStats, scrollState: ScrollState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Total Pnl ${uiState.totalPnl}")
        Text("Total Trades ${uiState.totalTrades}")
        Text("Total Wiining ${uiState.totalWinningTrades}")
        Text("Total Lossing ${uiState.totalLosingTrades}")
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeSuccess() {
    val scrollState = null
    scrollState?.let {
        HomeContent(
        HomeUiState.Success(
            stats = HomeStats(
                totalPnl = 1500.0,
                totalTrades = 10,
                totalWinningTrades = 6,
                totalLosingTrades = 4,
                winRate = 60.0,
                avgProfit = 300.0,
                avgLoss = -150.0,
                riskRewardRatio = 2.0,
                profitPercentage = 60.0
            ),
            trades = emptyList()
        ),
            it
    )
    }
}