package com.wallstreet.presentation.analytics.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.wallstreet.presentation.analytics.AnalyticsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun Calender(
    viewModel: AnalyticsViewModel = koinViewModel()

) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row() { }
    }

}