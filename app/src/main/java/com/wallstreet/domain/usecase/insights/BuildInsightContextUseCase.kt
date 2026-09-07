package com.wallstreet.domain.usecase.insights

import com.wallstreet.core.constants.AppConstants
import com.wallstreet.core.util.TradeMath
import com.wallstreet.domain.model.TimePeriod
import com.wallstreet.domain.model.Trade
import com.wallstreet.domain.model.insights.InsightContext
import com.wallstreet.domain.model.insights.MistakeComparison
import com.wallstreet.domain.model.insights.WindowStats
import com.wallstreet.domain.model.toDuration
import com.wallstreet.domain.usecase.analytics.GetDayPerformanceUseCase
import com.wallstreet.domain.usecase.analytics.GetOverviewStatsUseCase
import com.wallstreet.domain.usecase.analytics.GetTradeSummaryUseCase
import com.wallstreet.domain.usecase.analytics.GetTrendPerformanceUseCase
import com.wallstreet.domain.usecase.home.GetMistakesAnalysisUsecase
import com.wallstreet.domain.usecase.home.GetSymbolPerformanceUsecase
import com.wallstreet.domain.insights.copy.InsightThresholds
import java.time.LocalDate
import java.time.ZoneId

/**
 * Turns the user's full trade history into an [InsightContext]: a current window plus the
 * equal-length prior window (`null` when there isn't enough history), per-mistake comparisons
 * and personalization. Pure — [now] is injectable for deterministic tests.
 */
class BuildInsightContextUseCase(
    private val getOverviewStats: GetOverviewStatsUseCase,
    private val getTradeSummary: GetTradeSummaryUseCase,
    private val getTrendPerformance: GetTrendPerformanceUseCase,
    private val getDayPerformance: GetDayPerformanceUseCase,
    private val getMistakesAnalysis: GetMistakesAnalysisUsecase,
    private val getSymbolPerformance: GetSymbolPerformanceUsecase,
) {

    operator fun invoke(
        allTrades: List<Trade>,
        period: TimePeriod,
        roles: List<String>,
        currencySymbol: String,
        now: LocalDate = LocalDate.now(),
    ): InsightContext {
        val zone = ZoneId.systemDefault()
        val nowMs = now.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

        val current: WindowStats
        val previous: WindowStats?

        if (period == TimePeriod.ALL) {
            val earliestMs = allTrades.minOfOrNull { it.tradeDate } ?: nowMs
            current = buildWindow(allTrades, earliestMs, nowMs)
            previous = null
        } else {
            val dur = period.toDuration()
            val curStartMs = now.minus(dur).atStartOfDay(zone).toInstant().toEpochMilli()
            val prevStartMs = now.minus(dur).minus(dur).atStartOfDay(zone).toInstant().toEpochMilli()

            val currentTrades = allTrades.filter { it.tradeDate in curStartMs until nowMs }
            val previousTrades = allTrades.filter { it.tradeDate in prevStartMs until curStartMs }

            current = buildWindow(currentTrades, curStartMs, nowMs)

            val enoughPrev = previousTrades.size >= InsightThresholds.MIN_PREV_WINDOW_TRADES
            val historyReachesBack =
                allTrades.isNotEmpty() && allTrades.minOf { it.tradeDate } <= prevStartMs
            previous = if (enoughPrev && historyReachesBack) {
                buildWindow(previousTrades, prevStartMs, curStartMs)
            } else {
                null
            }
        }

        val comparisons = AppConstants.mistakes.mapNotNull { name ->
            val cur = current.mistakes.topMistakes.firstOrNull { it.name == name }
            val prv = previous?.mistakes?.topMistakes?.firstOrNull { it.name == name }
            if (cur == null && prv == null) return@mapNotNull null
            MistakeComparison(
                name = name,
                count = cur?.count ?: 0,
                prevCount = prv?.count ?: 0,
                totalImpact = cur?.totalPnlImpact ?: 0.0,
                prevImpact = prv?.totalPnlImpact ?: 0.0,
                winRate = cur?.winRate ?: 0.0,
            )
        }

        return InsightContext(
            current = current,
            previous = previous,
            mistakeComparisons = comparisons,
            overallMistakeRate = mistakeRate(current),
            prevOverallMistakeRate = previous?.let { mistakeRate(it) },
            roles = roles,
            currencySymbol = currencySymbol,
            period = period,
        )
    }

    private fun mistakeRate(w: WindowStats): Double =
        if (w.closedCount == 0) 0.0
        else w.mistakes.totalMistakeTrades.toDouble() / w.closedCount

    private fun buildWindow(trades: List<Trade>, startMs: Long, endMs: Long): WindowStats =
        WindowStats(
            trades = trades,
            closedCount = trades.count { it.profitLoss != null },
            windowStartMs = startMs,
            windowEndMs = endMs,
            overview = getOverviewStats(trades),
            summary = getTradeSummary(trades),
            trend = getTrendPerformance(trades),
            day = getDayPerformance(trades),
            mistakes = getMistakesAnalysis(trades),
            symbols = getSymbolPerformance(trades),
            grossProfit = TradeMath.grossProfit(trades),
            grossLoss = TradeMath.grossLoss(trades),
            profitFactor = TradeMath.profitFactor(trades),
            expectancy = TradeMath.expectancy(trades),
            avgWin = TradeMath.avgWin(trades),
            avgLoss = TradeMath.avgLoss(trades),
            maxDrawdown = TradeMath.maxDrawdown(trades),
            winStreak = TradeMath.winStreak(trades),
            lossStreak = TradeMath.lossStreak(trades),
            currentStreak = TradeMath.currentStreak(trades),
        )
}
