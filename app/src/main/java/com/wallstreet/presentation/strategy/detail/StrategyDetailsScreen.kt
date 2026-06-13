package com.wallstreet.presentation.strategy.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wallstreet.domain.model.TimePeriod
import com.wallstreet.presentation.analytics.components.DayWisePerformance
import com.wallstreet.presentation.analytics.components.LongShortCard
import com.wallstreet.presentation.analytics.components.TrendPerformanceCard
import com.wallstreet.presentation.home.components.EquityCurveChart
import com.wallstreet.presentation.home.components.PeriodSelector
import com.wallstreet.presentation.home.components.SymbolPerformanceCard
import com.wallstreet.presentation.home.components.TopMistakesCard
import com.wallstreet.presentation.strategy.detail.components.StrategyHeaderCard
import com.wallstreet.presentation.strategy.detail.components.StrategyRiskRewardCard
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrategyDetailsScreen(
    strategyId: String,
    onBack: () -> Unit,
    viewModel: StrategyDetailViewModel = koinViewModel { parametersOf(strategyId) }
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {
                    val title = (uiState as? StrategyDetailUiState.Success)?.strategy?.name
                        ?: "Strategy"
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        when (val state = uiState) {
            StrategyDetailUiState.Loading -> LoadingView(padding)
            is StrategyDetailUiState.Error -> ErrorView(state.message, padding)
            is StrategyDetailUiState.Success -> StrategyDetailSuccess(
                state = state,
                padding = padding,
                selectedPeriod = selectedPeriod,
                onPeriodSelected = viewModel::onPeriodSelected
            )
        }
    }
}

@Composable
private fun LoadingView(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorView(message: String, padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StrategyDetailSuccess(
    state: StrategyDetailUiState.Success,
    padding: PaddingValues,
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(scrollState)
            .padding(horizontal = 12.dp)
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        StrategyHeaderCard(
            strategy = state.strategy,
            totalTradesInPeriod = state.totalTradesInPeriod
        )

        // Section 1 — Period filter
        PeriodSelector(
            selectedPeriod = selectedPeriod,
            onPeriodSelected = onPeriodSelected
        )

        StrategyRiskRewardCard(
            stats = state.stats,
            profitFactor = state.profitFactor,
            maxDrawdown = state.maxDrawdown,
            winStreak = state.winStreak
        )
        EquityCurveChart(
            equityCurveData = state.equityCurveData,
            selectedPeriod = selectedPeriod,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        LongShortCard(summary = state.tradeSummary)
        SymbolPerformanceCard(symbols = state.symbolPerformance)
        DayWisePerformance(dayPerformance = state.dayPerformance)
        TrendPerformanceCard(data = state.trendPerformance)
        TopMistakesCard(
            data = state.mistakesAnalysisData,
            onViewAll = {}
        )
    }
}
