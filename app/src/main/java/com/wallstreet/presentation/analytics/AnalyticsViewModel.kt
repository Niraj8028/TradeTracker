package com.wallstreet.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallstreet.core.preferences.CurrencyPreferences
import com.wallstreet.core.result.Result
import com.wallstreet.domain.analytics.AnalyticsManager
import com.wallstreet.domain.insights.InsightEngine
import com.wallstreet.domain.model.DayPerformance
import com.wallstreet.domain.model.MistakesAnalysisData
import com.wallstreet.domain.model.OverviewStats
import com.wallstreet.domain.model.RecentTradeItem
import com.wallstreet.domain.model.TimePeriod
import com.wallstreet.domain.model.Trade
import com.wallstreet.domain.model.TradeSummary
import com.wallstreet.domain.model.TrendPerformanceData
import com.wallstreet.domain.model.insights.Insight
import com.wallstreet.domain.model.insights.InsightCategory
import com.wallstreet.domain.model.insights.MistakeComparison
import com.wallstreet.domain.repository.AuthRepository
import com.wallstreet.domain.repository.UserRepository
import com.wallstreet.domain.usecase.analytics.GetDayPerformanceUseCase
import com.wallstreet.domain.usecase.analytics.GetOverviewStatsUseCase
import com.wallstreet.domain.usecase.analytics.GetTradeSummaryUseCase
import com.wallstreet.domain.usecase.analytics.GetTrendPerformanceUseCase
import com.wallstreet.domain.usecase.home.GetMistakesAnalysisUsecase
import com.wallstreet.domain.usecase.home.getRecentTradeData
import com.wallstreet.domain.usecase.insights.BuildInsightContextUseCase
import com.wallstreet.domain.usecase.trade.GetTradesUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyticsViewModel(
    private val authRepository: AuthRepository,
    private val getTradeSummaryUseCase: GetTradeSummaryUseCase,
    private val getDayPerformanceUseCase: GetDayPerformanceUseCase,
    private val getTradesUseCase: GetTradesUseCase,
    private val getTrendPerformanceUseCase: GetTrendPerformanceUseCase,
    private val getOverviewStatsUseCase: GetOverviewStatsUseCase,
    private val getMistakesAnalysisUsecase: GetMistakesAnalysisUsecase,
    private val userRepository: UserRepository,
    private val buildInsightContext: BuildInsightContextUseCase,
    private val insightEngine: InsightEngine,
    private val currencyPreferences: CurrencyPreferences,
    private val analyticsManager: AnalyticsManager,
    private val computeDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {

    private val _selectedTabIndex = MutableStateFlow(0)
    private val _selectedFilter = MutableStateFlow(TimePeriod.ONE_MONTH)

    private data class Personalization(val roles: List<String>, val symbol: String)

    private data class AnalyticsData(
        val filter: TimePeriod,
        val tradeSummary: TradeSummary,
        val dayPerformance: DayPerformance,
        val allTrades: List<Trade>,
        val recentTrades: List<RecentTradeItem>,
        val trendPerformance: TrendPerformanceData,
        val overviewStats: OverviewStats,
        val mistakesAnalysis: MistakesAnalysisData,
        val insightsByCategory: Map<InsightCategory, List<Insight>>,
        val mistakeComparisons: List<MistakeComparison>,
    )

    private fun personalizationFlow(userId: String): Flow<Personalization> = flow {
        val roles = (userRepository.getAccountPrefs(userId) as? Result.Success)?.data?.roles.orEmpty()
        val symbol = runCatching { currencyPreferences.currencySymbol.first() }.getOrDefault("$")
        emit(Personalization(roles, symbol))
    }.catch { emit(Personalization(emptyList(), "$")) }

    /** Heavy, tab-independent: recomputes only on filter or trade-data change. */
    private val analyticsData: Flow<AnalyticsData> = _selectedFilter.flatMapLatest { filter ->
        val userId = authRepository.getCurrentUser()?.id ?: return@flatMapLatest emptyFlow()
        combine(
            getTradesUseCase(userId, filter, 500).distinctUntilChanged(),
            getTradesUseCase(userId, TimePeriod.ALL, 5000).distinctUntilChanged(),
            personalizationFlow(userId),
        ) { windowTrades, allTrades, personal ->
            val ctx = buildInsightContext(allTrades, filter, personal.roles, personal.symbol)
            val insights = insightEngine.run(ctx).byCategory
            AnalyticsData(
                filter = filter,
                tradeSummary = getTradeSummaryUseCase(windowTrades),
                dayPerformance = getDayPerformanceUseCase(windowTrades),
                allTrades = allTrades,
                recentTrades = getRecentTradeData(windowTrades),
                trendPerformance = getTrendPerformanceUseCase(windowTrades),
                overviewStats = getOverviewStatsUseCase(windowTrades),
                mistakesAnalysis = getMistakesAnalysisUsecase(windowTrades),
                insightsByCategory = insights,
                mistakeComparisons = ctx.mistakeComparisons,
            )
        }
    }.flowOn(computeDispatcher)

    private val loggedInsightIds = mutableSetOf<String>()

    val uiState: StateFlow<AnalyticsUiState> =
        combine(analyticsData, _selectedTabIndex) { data, tab ->
            AnalyticsUiState.Success(
                selectedFilter = data.filter,
                tradeSummary = data.tradeSummary,
                dayPerformance = data.dayPerformance,
                allTrades = data.allTrades,
                selectedTabIndex = tab,
                recentTrades = data.recentTrades,
                trendPerformance = data.trendPerformance,
                overviewStats = data.overviewStats,
                mistakesAnalysis = data.mistakesAnalysis,
                insightsByCategory = data.insightsByCategory,
                mistakeComparisons = data.mistakeComparisons,
            ) as AnalyticsUiState
        }
            .onStart { emit(AnalyticsUiState.Loading) }
            .onEach { if (it is AnalyticsUiState.Success) logNewImpressions(it) }
            .catch { e -> emit(AnalyticsUiState.Error(e.message ?: "Unknown error")) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = AnalyticsUiState.Loading,
            )

    private fun logNewImpressions(state: AnalyticsUiState.Success) {
        state.insightsByCategory.values.flatten().forEach { insight ->
            if (loggedInsightIds.add(insight.id)) {
                analyticsManager.logEvent(
                    "insight_shown",
                    mapOf(
                        "id" to insight.id,
                        "category" to insight.category.name,
                        "severity" to insight.severity.name,
                    ),
                )
            }
        }
    }

    fun onTabSelect(index: Int) {
        _selectedTabIndex.value = index
    }

    fun onSelectFilter(filter: TimePeriod) {
        loggedInsightIds.clear()
        _selectedFilter.value = filter
    }
}
