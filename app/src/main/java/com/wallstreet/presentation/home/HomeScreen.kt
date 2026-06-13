package com.wallstreet.presentation.home

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import com.wallstreet.domain.model.EquityPoint
import com.wallstreet.domain.model.TimePeriod
import com.wallstreet.presentation.home.components.EquityCurveChart
import com.wallstreet.presentation.home.components.HeatMapCard
import com.wallstreet.presentation.home.components.RecentTradesSection
import com.wallstreet.presentation.home.components.StatsRow
import com.wallstreet.presentation.home.components.MainPnLCard
import com.wallstreet.presentation.home.components.SymbolPerformanceCard
import com.wallstreet.presentation.home.components.TopMistakesCard
import org.koin.androidx.compose.koinViewModel


@Composable
fun HomeScreen(viewModel: HomeViewModel = koinViewModel()) {
    val uiState by viewModel.homeUiState.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val scrollState = rememberScrollState()

    HomeContent(
        uiState,
        scrollState,
        selectedPeriod = selectedPeriod,
        onPeriodSelected = viewModel::onPeriodSelected,
        onDeleteTrade = viewModel::deleteTrade
    )
}

@Composable
fun HomeContent(
    uiState: HomeUiState,
    scrollState: ScrollState,
    onPeriodSelected: (TimePeriod) -> Unit,
    selectedPeriod: TimePeriod,
    onDeleteTrade: (String) -> Unit = {}
) {
    when (uiState) {
        is HomeUiState.Error -> {
            Text("ErrorView ${uiState.error}", style = MaterialTheme.typography.bodyMedium)
        }

        HomeUiState.Loading -> {
            LoadingView()
        }

        is HomeUiState.Success -> {
            SuccessView(uiState, scrollState, selectedPeriod, onPeriodSelected = onPeriodSelected, onDeleteTrade = onDeleteTrade)
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
fun SuccessView(
    uiState: HomeUiState.Success,
    scrollState: ScrollState,
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit,
    onDeleteTrade: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 12.dp)
            .padding(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        MainPnLCard(
            stats = uiState.stats,
            selectedPeriod = selectedPeriod,
            onPeriodSelected = onPeriodSelected
        )
        StatsRow(stats = uiState.stats)
        EquityCurveChart(
            equityCurveData = uiState.equityCurveData,
            selectedPeriod = selectedPeriod,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        HeatMapCard(heatMapData = uiState.heatMapData)
        TopMistakesCard(data = uiState.mistakesAnalysisData)
        SymbolPerformanceCard(symbols = uiState.symbolPerformance)
        RecentTradesSection(
            trades = uiState.recentTrades,
            onViewAll = { /*TODO*/ },
            onDeleteTrade = onDeleteTrade
        )
    }
}
