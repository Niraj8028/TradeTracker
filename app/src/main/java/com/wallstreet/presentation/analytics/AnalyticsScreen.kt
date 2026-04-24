package com.wallstreet.presentation.analytics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.wallstreet.presentation.analytics.components.AppTabRow
import com.wallstreet.presentation.analytics.components.Calendar
import com.wallstreet.presentation.analytics.components.OverView
import com.wallstreet.presentation.analytics.components.TabItem
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel = koinViewModel()) {
    val tabList = listOf<TabItem>(TabItem("OverView"), TabItem("Calender"))
    val selectedTabIndex by viewModel.selectedTabIndex.collectAsState()
    val pagerState = rememberPagerState() { tabList.size }
    LaunchedEffect(pagerState.currentPage) {
        viewModel.onTabSelect(pagerState.currentPage)
    }
    LaunchedEffect(selectedTabIndex) {
        pagerState.animateScrollToPage(selectedTabIndex)
    }
    Scaffold(

        containerColor = MaterialTheme.colorScheme.background
    ) { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()

        ) {

            AppTabRow(
                tabs = tabList,
                selectedIndex = selectedTabIndex,
                onTabChange =
                    viewModel::onTabSelect
            )
            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                when (page) {
                    0 -> OverView()
                    1 -> Calendar()
                }
            }

        }
    }
}



