package com.wallstreet.presentation.analytics.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.patrykandpatrick.vico.compose.common.component.shadow
import com.wallstreet.R
import com.wallstreet.ui.theme.BorderColors
import com.wallstreet.ui.theme.LocalBorderColors

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
        modifier = Modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
//
//        Text("${currentMonth.month} ${currentMonth.year}")
//


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
                    Text(
                        day, style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                    .then(
                        if (day.isToday)
                            Modifier.border(
                                1.dp,
                                LocalBorderColors.current.secondary,
                                CircleShape,

                                )
                        else Modifier
                    )
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(modifier = Modifier, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        day.date.dayOfMonth.toString(),
                        color = if (day.isCurrentMonth) MaterialTheme.colorScheme.onBackground else LocalBorderColors.current.secondary
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Image(
                        painter = painterResource(id = R.drawable.dot),
                        contentDescription = null,
                        modifier = Modifier.size(8.dp),
                        colorFilter = ColorFilter.tint(Color.Red)
                    )
                }

            }
        }
    }
}