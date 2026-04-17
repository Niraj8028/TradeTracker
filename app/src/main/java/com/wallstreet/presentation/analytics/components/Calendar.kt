package com.wallstreet.presentation.analytics.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wallstreet.presentation.analytics.AnalyticsViewModel
import com.wallstreet.presentation.analytics.CalenderDay
import org.koin.androidx.compose.koinViewModel
import java.time.YearMonth
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment

@Composable
fun Calendar(
    viewModel: AnalyticsViewModel = koinViewModel()

) {

    val initialPage = 500
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { 1000 }
    )

    val baseMonth = remember { YearMonth.now() }

    fun pageToMonth(page: Int): YearMonth {
        return baseMonth.plusMonths((page - initialPage).toLong())
    }

    val currentMonth by remember {
        derivedStateOf {
            pageToMonth(pagerState.currentPage)
        }
    }

    Column(
        modifier = Modifier
            .padding(19.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(8.dp)
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("${currentMonth.month} ${currentMonth.year}")

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            userScrollEnabled = false
        ) {
            items(viewModel.days) { day ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(day)
                }
            }
        }


        HorizontalPager(
            state = pagerState,
        ) { page ->

            val yearMonth = pageToMonth(page)

            val days = remember(yearMonth) {
                viewModel.generateMonth(yearMonth)
            }

            CalendarGrid(days = days)
        }
    }

}

@Composable
fun CalendarGrid(days: List<CalenderDay>) {

    LazyVerticalGrid(columns = GridCells.Fixed(7)) {

        items(days) { day ->

            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .padding(1.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    day.date.dayOfMonth.toString(),
                    color = if (day.isCurrentMonth) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}