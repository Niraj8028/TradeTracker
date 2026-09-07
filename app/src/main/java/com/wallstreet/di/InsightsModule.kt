package com.wallstreet.di

import com.wallstreet.domain.analytics.AnalyticsManager
import com.wallstreet.domain.insights.InsightEngine
import com.wallstreet.domain.insights.detectors.analyticsDetectors
import com.wallstreet.domain.insights.detectors.strategyDetectors
import com.wallstreet.domain.usecase.insights.BuildInsightContextUseCase
import com.wallstreet.domain.usecase.strategy.GetStrategyInsightsUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val insightsModule = module {
    // Background dispatcher for the ViewModels' heavy insight/stat flows (overridable in tests).
    factory<CoroutineDispatcher> { Dispatchers.Default }

    single {
        InsightEngine(
            detectors = analyticsDetectors,
            strategyDetectors = strategyDetectors,
            analytics = getOrNull<AnalyticsManager>(),
        )
    }
    factory { BuildInsightContextUseCase(get(), get(), get(), get(), get(), get()) }
    factory { GetStrategyInsightsUseCase(get(), get(), get(), get()) }
}
