package com.wallstreet.presentation.analytics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wallstreet.presentation.analytics.components.Calendar
import org.koin.androidx.compose.koinViewModel


@Composable
fun AnalyticsScreen(
) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(10.dp)) {
        Calendar()
    }

}



