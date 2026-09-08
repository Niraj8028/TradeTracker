package com.wallstreet.testutil

import com.wallstreet.data.repository.AnalyticsRepositoryImp
import com.wallstreet.domain.model.TimePeriod
import com.wallstreet.domain.model.Trade
import com.wallstreet.domain.model.insights.InsightContext
import com.wallstreet.domain.usecase.analytics.GetDayPerformanceUseCase
import com.wallstreet.domain.usecase.analytics.GetOverviewStatsUseCase
import com.wallstreet.domain.usecase.analytics.GetTradeSummaryUseCase
import com.wallstreet.domain.usecase.analytics.GetTrendPerformanceUseCase
import com.wallstreet.domain.usecase.home.GetMistakesAnalysisUsecase
import com.wallstreet.domain.usecase.home.GetSymbolPerformanceUsecase
import com.wallstreet.domain.usecase.insights.BuildInsightContextUseCase
import java.time.LocalDate

/** Real (pure) analytics use cases wired up so tests build a genuine [InsightContext]. */
object InsightContexts {

    private val repo = AnalyticsRepositoryImp()

    val builder = BuildInsightContextUseCase(
        getOverviewStats = GetOverviewStatsUseCase(repo),
        getTradeSummary = GetTradeSummaryUseCase(repo),
        getTrendPerformance = GetTrendPerformanceUseCase(repo),
        getDayPerformance = GetDayPerformanceUseCase(repo),
        getMistakesAnalysis = GetMistakesAnalysisUsecase(),
        getSymbolPerformance = GetSymbolPerformanceUsecase(),
    )

    fun of(
        trades: List<Trade>,
        period: TimePeriod = TimePeriod.ONE_MONTH,
        roles: List<String> = emptyList(),
        symbol: String = "$",
        now: LocalDate = LocalDate.now(),
    ): InsightContext = builder(trades, period, roles, symbol, now)
}
