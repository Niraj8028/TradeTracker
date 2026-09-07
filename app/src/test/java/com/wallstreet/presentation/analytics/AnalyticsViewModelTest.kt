package com.wallstreet.presentation.analytics

import app.cash.turbine.test
import com.wallstreet.core.preferences.CurrencyPreferences
import com.wallstreet.core.result.Result
import com.wallstreet.data.repository.AnalyticsRepositoryImp
import com.wallstreet.domain.analytics.AnalyticsManager
import com.wallstreet.domain.insights.InsightEngine
import com.wallstreet.domain.insights.InsightResult
import com.wallstreet.domain.model.TimePeriod
import com.wallstreet.domain.model.User
import com.wallstreet.domain.model.insights.Insight
import com.wallstreet.domain.model.insights.InsightCategory
import com.wallstreet.domain.model.insights.InsightContext
import com.wallstreet.domain.model.insights.InsightSeverity
import com.wallstreet.domain.repository.AccountPrefs
import com.wallstreet.domain.repository.AuthRepository
import com.wallstreet.domain.repository.UserRepository
import com.wallstreet.domain.usecase.analytics.GetDayPerformanceUseCase
import com.wallstreet.domain.usecase.analytics.GetOverviewStatsUseCase
import com.wallstreet.domain.usecase.analytics.GetTradeSummaryUseCase
import com.wallstreet.domain.usecase.analytics.GetTrendPerformanceUseCase
import com.wallstreet.domain.usecase.home.GetMistakesAnalysisUsecase
import com.wallstreet.domain.usecase.insights.BuildInsightContextUseCase
import com.wallstreet.domain.usecase.trade.GetTradesUseCase
import com.wallstreet.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AnalyticsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val analyticsRepo = AnalyticsRepositoryImp()
    private val authRepository = mockk<AuthRepository>()
    private val getTradesUseCase = mockk<GetTradesUseCase>()
    private val userRepository = mockk<UserRepository>()
    private val buildInsightContext = mockk<BuildInsightContextUseCase>()
    private val insightEngine = mockk<InsightEngine>()
    private val currencyPreferences = mockk<CurrencyPreferences>()
    private val analyticsManager = mockk<AnalyticsManager>(relaxed = true)

    private val sampleInsight = Insight(
        id = "mistake.costliest",
        category = InsightCategory.MISTAKES,
        severity = InsightSeverity.WARNING,
        title = "FOMO",
        body = "FOMO cost you money.",
        priority = 0.8,
    )

    private fun viewModel() = AnalyticsViewModel(
        authRepository = authRepository,
        getTradeSummaryUseCase = GetTradeSummaryUseCase(analyticsRepo),
        getDayPerformanceUseCase = GetDayPerformanceUseCase(analyticsRepo),
        getTradesUseCase = getTradesUseCase,
        getTrendPerformanceUseCase = GetTrendPerformanceUseCase(analyticsRepo),
        getOverviewStatsUseCase = GetOverviewStatsUseCase(analyticsRepo),
        getMistakesAnalysisUsecase = GetMistakesAnalysisUsecase(),
        userRepository = userRepository,
        buildInsightContext = buildInsightContext,
        insightEngine = insightEngine,
        currencyPreferences = currencyPreferences,
        analyticsManager = analyticsManager,
    )

    private fun stubHappyPath(roles: List<String> = listOf("Forex")) {
        every { authRepository.getCurrentUser() } returns User("u1", "Nia", "n@x.com", null)
        every { getTradesUseCase(any(), any(), any()) } returns flowOf(emptyList())
        every { currencyPreferences.currencySymbol } returns flowOf("$")
        coEvery { userRepository.getAccountPrefs("u1") } returns
            Result.Success(AccountPrefs(true, "USD", roles))
        every { buildInsightContext(any(), any(), any(), any(), any()) } returns
            mockk<InsightContext>(relaxed = true)
        every { insightEngine.run(any()) } returns InsightResult(
            all = listOf(sampleInsight),
            byCategory = mapOf(InsightCategory.MISTAKES to listOf(sampleInsight)),
        )
    }

    @Test
    fun `emits Loading then Success carrying engine insights`() = runTest {
        stubHappyPath()
        viewModel().uiState.test {
            assertEquals(AnalyticsUiState.Loading, awaitItem())
            val success = awaitItem() as AnalyticsUiState.Success
            assertEquals(
                listOf(sampleInsight),
                success.insightsByCategory[InsightCategory.MISTAKES],
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a roles fetch failure still yields Success with no roles`() = runTest {
        every { authRepository.getCurrentUser() } returns User("u1", "Nia", "n@x.com", null)
        every { getTradesUseCase(any(), any(), any()) } returns flowOf(emptyList())
        every { currencyPreferences.currencySymbol } returns flowOf("$")
        coEvery { userRepository.getAccountPrefs("u1") } throws RuntimeException("offline")
        every { buildInsightContext(any(), any(), emptyList(), any()) } returns
            mockk<InsightContext>(relaxed = true)
        every { insightEngine.run(any()) } returns InsightResult(emptyList(), emptyMap())

        viewModel().uiState.test {
            assertEquals(AnalyticsUiState.Loading, awaitItem())
            assertTrue(awaitItem() is AnalyticsUiState.Success)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `switching tabs does not rebuild the insight context`() = runTest {
        stubHappyPath()
        val vm = viewModel()
        vm.uiState.test {
            awaitItem() // Loading
            val first = awaitItem() as AnalyticsUiState.Success
            assertEquals(0, first.selectedTabIndex)
            vm.onTabSelect(1)
            val afterTab = awaitItem() as AnalyticsUiState.Success
            assertEquals(1, afterTab.selectedTabIndex)
            verify(exactly = 1) { buildInsightContext(any(), any(), any(), any(), any()) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `changing the filter recomputes`() = runTest {
        stubHappyPath()
        val vm = viewModel()
        vm.uiState.test {
            awaitItem() // Loading
            awaitItem() // Success @ 1M
            vm.onSelectFilter(TimePeriod.THREE_MONTHS)
            val next = awaitItem() as AnalyticsUiState.Success
            assertEquals(TimePeriod.THREE_MONTHS, next.selectedFilter)
            verify(exactly = 2) { buildInsightContext(any(), any(), any(), any(), any()) }
            cancelAndIgnoreRemainingEvents()
        }
    }
}
