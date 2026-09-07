package com.wallstreet.di

import com.wallstreet.domain.analytics.AnalyticsManager
import com.wallstreet.domain.insights.InsightEngine
import com.wallstreet.domain.insights.detectors.analyticsDetectors
import com.wallstreet.domain.insights.detectors.strategyDetectors
import com.wallstreet.domain.usecase.insights.BuildInsightContextUseCase
import com.wallstreet.domain.usecase.strategy.GetStrategyInsightsUseCase
import org.koin.dsl.module

val insightsModule = module {
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
