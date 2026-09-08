package com.wallstreet.presentation.strategy

import app.cash.turbine.test
import com.wallstreet.core.preferences.CurrencyPreferences
import com.wallstreet.core.result.Result
import com.wallstreet.domain.analytics.AnalyticsManager
import com.wallstreet.domain.model.Strategy
import com.wallstreet.domain.model.TimePeriod
import com.wallstreet.domain.model.User
import com.wallstreet.domain.model.insights.Insight
import com.wallstreet.domain.model.insights.InsightCategory
import com.wallstreet.domain.model.insights.InsightSeverity
import com.wallstreet.domain.model.insights.StrategyVerdict
import com.wallstreet.domain.model.strategy.StrategyStats
import com.wallstreet.domain.repository.AccountPrefs
import com.wallstreet.domain.repository.AuthRepository
import com.wallstreet.domain.repository.UserRepository
import com.wallstreet.domain.usecase.strategy.AddStrategyUseCase
import com.wallstreet.domain.usecase.strategy.DeleteStrategyUseCase
import com.wallstreet.domain.usecase.strategy.GetStrategyInsightsUseCase
import com.wallstreet.domain.usecase.strategy.GetStrategyStatsUsecase
import com.wallstreet.domain.usecase.strategy.StrategyInsightsResult
import com.wallstreet.domain.usecase.strategy.UpdateStrategyUseCase
import com.wallstreet.testutil.MainDispatcherRule
import io.mockk.every
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class StrategyViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val auth = mockk<AuthRepository>()
    private val stats = mockk<GetStrategyStatsUsecase>()
    private val insights = mockk<GetStrategyInsightsUseCase>()
    private val userRepository = mockk<UserRepository>()
    private val currencyPreferences = mockk<CurrencyPreferences>()
    private val analyticsManager = mockk<AnalyticsManager>(relaxed = true)

    private val strategy = Strategy(id = "s1", name = "Breakout")
    private val statList = listOf(
        StrategyStats(strategy, totalTrades = 12, totalPnl = 900.0, winRate = 60.0,
            rrRatio = 2.0, avgProfitPerTrade = 75.0, period = TimePeriod.ONE_MONTH),
    )
    private val headline = Insight(
        id = "strategy.scaleUp:s1", category = InsightCategory.STRATEGY,
        severity = InsightSeverity.POSITIVE, title = "Breakout",
        body = "Breakout is your edge.", priority = 0.9,
        strategyId = "s1", verdict = StrategyVerdict.SCALE_UP,
    )
    private val insightsResult = StrategyInsightsResult(
        headline = headline,
        verdictByStrategyId = mapOf("s1" to StrategyVerdict.SCALE_UP),
        insightsByStrategyId = mapOf("s1" to listOf(headline)),
    )

    private fun viewModel(): StrategyViewModel {
        every { auth.getCurrentUser() } returns User("u1", "Nia", "n@x.com", null)
        coEvery { userRepository.getAccountPrefs("u1") } returns
            Result.Success(AccountPrefs(true, "USD", listOf("Forex")))
        every { currencyPreferences.currencySymbol } returns flowOf("$")
        every { stats(any(), any()) } returns flowOf(statList)
        every { insights(any(), any(), any(), any(), any()) } returns flowOf(insightsResult)
        return StrategyViewModel(
            updateStrategyUseCase = mockk<UpdateStrategyUseCase>(relaxed = true),
            deleteStrategyUseCase = mockk<DeleteStrategyUseCase>(relaxed = true),
            addStrategyUseCase = mockk<AddStrategyUseCase>(relaxed = true),
            getStrategyStatsUsecase = stats,
            getStrategyInsightsUseCase = insights,
            userRepository = userRepository,
            currencyPreferences = currencyPreferences,
            analyticsManager = analyticsManager,
            authRepository = auth,
            computeDispatcher = mainDispatcherRule.dispatcher,
        )
    }

    @Test
    fun `success carries strategy insights and verdicts`() = runTest {
        viewModel().uiState.test {
            awaitItem() // Loading
            val success = awaitItem() as StrategiesUiState.Success
            assertEquals(listOf(headline), success.strategyInsights)
            assertEquals(StrategyVerdict.SCALE_UP, success.verdictByStrategyId["s1"])
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `changing the period re-emits`() = runTest {
        val vm = viewModel()
        vm.uiState.test {
            awaitItem() // Loading
            awaitItem() // Success @ 1M
            vm.onPeriodSelected(TimePeriod.THREE_MONTHS)
            skipItems(1) // Loading again (inner flow restarts)
            val next = awaitItem() as StrategiesUiState.Success
            assertEquals(TimePeriod.THREE_MONTHS, next.selectedPeriod)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `changing the sort keeps the same insights`() = runTest {
        val vm = viewModel()
        vm.uiState.test {
            awaitItem() // Loading
            val first = awaitItem() as StrategiesUiState.Success
            vm.onSortSelected(StrategySortOption.WIN_RATE)
            skipItems(1) // Loading again
            val sorted = awaitItem() as StrategiesUiState.Success
            assertEquals(first.strategyInsights, sorted.strategyInsights)
            assertEquals(first.verdictByStrategyId, sorted.verdictByStrategyId)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
