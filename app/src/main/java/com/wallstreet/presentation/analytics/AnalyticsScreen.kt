package com.wallstreet.presentation.analytics

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wallstreet.presentation.analytics.components.AppTabRow
import com.wallstreet.presentation.analytics.components.FilterOption
import com.wallstreet.presentation.analytics.components.FilterTab
import com.wallstreet.presentation.analytics.components.MistakesTab
import com.wallstreet.presentation.analytics.components.OverView
import com.wallstreet.presentation.analytics.components.TabItem
import com.wallstreet.presentation.analytics.components.TrendTab
import org.koin.androidx.compose.koinViewModel

@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tabList = listOf(
        TabItem("Overview"),
        TabItem("Mistakes"),
        TabItem("Trend"),
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { _ ->
        when (val state = uiState) {
            is AnalyticsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is AnalyticsUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.error, color = MaterialTheme.colorScheme.error)
                }
            }

            is AnalyticsUiState.Success -> {
                AnalyticsContent(
                    state = state,
                    tabList = tabList,
                    onFilterSelect = { option -> viewModel.onSelectFilter(option.toTimePeriod()) },
                    onTabSelect = viewModel::onTabSelect
                )
            }
        }
    }
}

@Composable
private fun AnalyticsContent(
    state: AnalyticsUiState.Success,
    tabList: List<TabItem>,
    onFilterSelect: (FilterOption) -> Unit,
    onTabSelect: (Int) -> Unit
) {
    val pagerState = rememberPagerState(initialPage = state.selectedTabIndex) { tabList.size }

    LaunchedEffect(state.selectedTabIndex) {
        if (pagerState.currentPage != state.selectedTabIndex) {
            pagerState.animateScrollToPage(state.selectedTabIndex)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (state.selectedTabIndex != pagerState.currentPage) {
            onTabSelect(pagerState.currentPage)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AppTabRow(
            tabs = tabList,
            selectedIndex = state.selectedTabIndex,
            onTabChange = onTabSelect
        )
        FilterTab(
            filters = FilterOption.all,
            selected = FilterOption.fromTimePeriod(state.selectedFilter),
            onSelectFilter = onFilterSelect
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> OverView(
                    summary = state.tradeSummary,
                    overviewStats = state.overviewStats,
                    dayPerformance = state.dayPerformance,
                    recentTrades = state.recentTrades,
                    allTrades = state.allTrades
                )
                1 -> MistakesTab(data = state.mistakesAnalysis)
                2 -> TrendTab(data = state.trendPerformance)
            }
        }
    }
}
