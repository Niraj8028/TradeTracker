# Personalized Trade-Insights Engine

> **Status:** design under review — no implementation started.
> Note: the `develop` commit `f118b8b "Added remote configs for mistakes and strategies"` is
> misleadingly named — it only touched `.idea/deploymentTargetSelector.xml`, so the
> "copy = templates in code, no Remote Config" decision below still stands.

## Context

The "suggestions" the app shows today are three hand-written rule blocks buried inside Compose
files (`MistakeSuggestions.kt`, `buildInsights` in `TrendTab.kt`, `buildLongShortInsights` in
`LongShortInsightsCard.kt`). Copy is fixed strings, filtering is crude, and most users see the
same generic lines regardless of their data. There is no engine, no domain model for insights,
no personalization, and no tests.

This replaces that with a **deterministic, on-device insights engine** in the domain layer: one
`InsightContext` precomputed from the user's trades (current window + equal-length prior window
+ onboarding roles), fed to a set of small scored **detectors**, each emitting a structured
`Insight` with templated copy filled with the user's real numbers. The engine ranks, dedupes,
and caps. The UI becomes one reusable `InsightsCard`.

**Focus areas (per product):** MISTAKES — which mistake costs how much, how many mistakes
overall, which are repeating, which are reducing/cleared, which are new — and STRATEGY — which
strategy is working, which to keep vs drop. Directional / trend / day insights are ported but
kept light.

**Locked decisions:**
- v1 uses existing `Trade` fields only (no logging-form changes); detectors structured so
  per-trade R-multiple detectors can be added in phase 2.
- Personalization = onboarding **roles** + **current-window vs immediately-prior-equal-window**
  comparison. No recency weighting, no multi-bucket sparkline.
- Copy = **templates in code** (one registry) + threshold constants; structured for a later
  remote source. No Remote Config now.
- Analytics: feed the existing 3 tabs (Overview / Mistakes / Trend) via `InsightsCard`.
  **Redesign the Mistakes tab** around the engine. Overview + Trend just swap their hardcoded
  insight blocks for engine output.
- Strategy: verdicts on the **Strategy screens only** — a verdict badge on each `StrategyCard`,
  a headline insight atop the list, a per-strategy insights card on the detail screen.

## Verified codebase facts

- `Trade` (`domain/model/Trade.kt`): `id, symbol, entryPrice, exitPrice:Double?, quantity,
  strategyId:String?, tradeType(LONG/SHORT), tradeDate:Long, profitLoss:Double?, strategy:String
  (free text), notes, mistakes:List<String>, trendDirection:TrendDirection?`. Closed iff
  `profitLoss != null`. No stop/target/risk/fees/entry-exit-time.
- Existing pure sync use cases (`operator fun invoke(trades): X`, Koin `factory{}`):
  `GetOverviewStatsUseCase`, `GetTradeSummaryUseCase`, `GetTrendPerformanceUseCase`,
  `GetDayPerformanceUseCase`, `GetMistakesAnalysisUsecase`, `GetSymbolPerformanceUsecase`.
- `GetStrategyStatsUsecase(strategyRepository, tradeRepository)` → `Flow<List<StrategyStats>>`,
  groups trades by `strategyId` (skips blank), window via `period.toDuration()`. Its
  `computeStatsForStrategy` is the reusable math.
- `AnalyticsViewModel`: `_selectedFilter.flatMapLatest { combine(getTradesUseCase(userId,
  filter, 500), getTradesUseCase(userId, ALL, 5000), _selectedTabIndex) { ... } }`. The
  all-time list is uncapped; the windowed list is capped at 100 by a `GetTradesUseCase` bug
  (`limit` arg ignored for non-ALL). `AnalyticsScreen` renders `MistakesTab` / `TrendTab` /
  `OverView` per pager page.
- `LongShortInsightsCard` **is** wired — `OverView.kt:39`.
- Win definition diverges: `GetMistakesAnalysisUsecase` uses `pnl >= 0`; `TradeUtils`,
  `AnalyticsRepositoryImp`, `GetTrendPerformanceUseCase`, `GetStrategyStatsUsecase` use `> 0`.
- Roles: Firestore `users/{uid}.roles: List<String>`. `UserRepositoryImpl.getAccountPrefs`
  already reads it; `AccountPrefs(onboardingCompleted, currencyCode)` just doesn't expose it.
- `AnalyticsManager` is already a Koin `single`. `AnalyticsViewModel` / `StrategyViewModel`
  don't take it yet.
- Formatters: `Double.formatPnl(symbol)` (`core/util/TradeUtils.kt`), `Double.formatPercent()`
  (expects 0..100, `core/util/CurrencyUtils.kt`).
- Tests: none real (`com.example.myapplication` template only). No MockK / Turbine /
  coroutines-test in the version catalog.
- `StrategyDetailViewModel` computes profitFactor / maxDrawdown / winStreak inline (~L69-92) —
  duplicated math to extract.

## Architecture

### Package layout

```
domain/model/insights/      Insight.kt (Insight, InsightCategory, InsightSeverity, MetricChip,
                              StrategyVerdict), WindowStats.kt, InsightContext.kt, MistakeComparison.kt
domain/insights/            InsightDetector.kt, StrategyInsightDetector.kt, InsightEngine.kt
                              (InsightEngine, InsightEngineConfig, InsightResult), PriorityScore.kt,
                              Detectors.kt (analyticsDetectors / strategyDetectors lists)
domain/insights/copy/       InsightCopy.kt (template registry + moved curatedAdvice),
                              InsightThresholds.kt
domain/insights/detectors/  mistakes/ (9)  strategy/ (6)  direction/ (4)  timing/ risk/ discipline/ (5)
domain/usecase/insights/    BuildInsightContextUseCase.kt
domain/usecase/strategy/    GetStrategyInsightsUseCase.kt (+ StrategyInsightsResult),
                              StrategyStatsCalculator.kt (extracted)
core/util/                  TradeMath.kt
di/                         InsightsModule.kt
presentation/components/    InsightsCard.kt (shared: Analytics + Strategy)
```

Detectors are pure stateless objects held in top-level `val` lists — not DI beans. Only
`InsightEngine`, `BuildInsightContextUseCase`, `GetStrategyInsightsUseCase`,
`StrategyStatsCalculator` get Koin registrations.

`InsightEngine`, `BuildInsightContextUseCase` and every detector are **pure Kotlin with zero
Android / coroutine dependencies** — input `List<Trade>` (+ context), output `List<Insight>`.
That keeps them trivially unit-testable and lets the exact same code run from a ViewModel today
or a WorkManager `Worker` later (see *Performance & compute strategy*) without a rewrite.

### Core types (signatures)

```kotlin
enum class InsightCategory { MISTAKES, STRATEGY, DIRECTION, TIMING, RISK, DISCIPLINE }
enum class InsightSeverity { CRITICAL, WARNING, INFO, POSITIVE }        // → dot color
enum class StrategyVerdict { SCALE_UP, KEEP, REVIEW, DROP, NEEDS_MORE_DATA }

data class MetricChip(val label: String, val value: String, val tone: InsightSeverity = INFO)

data class Insight(
    val id: String,                    // stable detector id; per-entity detectors suffix ":{key}"
    val category: InsightCategory,
    val severity: InsightSeverity,
    val title: String,                 // short (mistake name / strategy name / "Losing streak")
    val body: String,                  // fully substituted, currency already formatted
    val priority: Double,              // 0..1
    val chips: List<MetricChip> = emptyList(),
    val evidenceTradeIds: List<String> = emptyList(),
    val strategyId: String? = null,    // STRATEGY only
    val verdict: StrategyVerdict? = null
)

data class WindowStats(
    val trades: List<Trade>, val closedCount: Int,
    val windowStartMs: Long, val windowEndMs: Long,
    val overview: OverviewStats, val summary: TradeSummary,
    val trend: TrendPerformanceData, val day: DayPerformance,
    val mistakes: MistakesAnalysisData, val symbols: List<SymbolStat>,
    // new, from TradeMath (win == pnl > 0):
    val grossProfit: Double, val grossLoss: Double, val profitFactor: Double?,
    val expectancy: Double, val avgWin: Double, val avgLoss: Double,
    val maxDrawdown: DrawdownResult,
    val winStreak: Int, val lossStreak: Int, val currentStreak: Int   // + wins / − losses
)

data class MistakeComparison(          // per canonical mistake name, current vs previous
    val name: String,
    val count: Int, val prevCount: Int,
    val totalImpact: Double, val prevImpact: Double,
    val winRate: Double
)

data class InsightContext(
    val current: WindowStats,
    val previous: WindowStats?,        // null = insufficient history
    val mistakeComparisons: List<MistakeComparison>,
    val overallMistakeRate: Double,    // taggedTrades / closedCount, current window (0..1)
    val prevOverallMistakeRate: Double?,
    val roles: List<String>,
    val currencySymbol: String,
    val period: TimePeriod
)

interface InsightDetector { val id: String; val category: InsightCategory
    fun detect(ctx: InsightContext): List<Insight> }               // 0..n, may be one per entity

interface StrategyInsightDetector { val id: String
    fun detect(ctx: StrategyInsightContext): List<Insight> }

data class InsightEngineConfig(val minPriority: Double = 0.15,
    val perCategoryCap: Int = 4, val globalCap: Int = 12)
data class InsightResult(val all: List<Insight>,
    val byCategory: Map<InsightCategory, List<Insight>>)

class InsightEngine(
    private val detectors: List<InsightDetector>,
    private val strategyDetectors: List<StrategyInsightDetector> = emptyList(),
    private val config: InsightEngineConfig = InsightEngineConfig(),
    private val analytics: AnalyticsManager? = null                // logError only
) {
    fun run(ctx: InsightContext): InsightResult
    fun runStrategy(ctx: StrategyInsightContext): InsightResult
}
```

`run()` pipeline: `flatMap { runCatching { it.detect(ctx) }.getOrElse { analytics?.logError(...); [] } }`
→ dedupe by `id` keeping max `priority` → filter `priority >= minPriority` →
`sortedWith(compareByDescending { priority }.thenBy { severity.ordinal })` →
`byCategory = groupBy { category }.mapValues { it.take(perCategoryCap) }` →
`all = byCategory.values.flatten().sortedByDescending { priority }.take(globalCap)`.

### Priority score (`PriorityScore.of`)

```
impactScore     = |financialImpact| / (|financialImpact| + IMPACT_SCALE)   // saturating 0..1
severityWeight  = CRITICAL 1.0 | WARNING 0.66 | POSITIVE 0.45 | INFO 0.33
confidenceScore = min(1.0, sampleSize / CONFIDENT_SAMPLE)                  // CONFIDENT_SAMPLE = 10
priority = clamp01(0.50*impactScore + 0.30*severityWeight + 0.20*confidenceScore + deltaBoost)
```

`IMPACT_SCALE` (currency units, default 500) and the weights live in `InsightThresholds` for
later remote tuning. Non-financial detectors pass `financialImpact = 0` and lean on severity +
confidence. `deltaBoost` (0..0.2) is used by current-vs-previous "movement" detectors.

### Copy registry (`InsightCopy`)

- `templates: Map<String,String>` keyed by detector id, `{token}` substitution via
  `render(key, args)`. All figures pre-formatted (`formatPnl` / `formatPercent`) by the detector.
- `mistakeAdvice: Map<String,String>` — the **8 `curatedAdvice` paragraphs moved verbatim** from
  `MistakeSuggestions.kt`; `advice(name)` returns the mapped paragraph or the existing generic
  fallback. Coaching mistake detectors append this as the `{advice}` token.

## Detector catalog (v1)

`title` is short; the `body` examples below carry the templated copy. All money via
`ctx.currencySymbol`.

### MISTAKES (9) — source: `ctx.current.mistakes`, `ctx.mistakeComparisons`, `ctx.overallMistakeRate`

| id | trigger | min-sample | severity | body example |
|----|---------|-----------|----------|--------------|
| `mistake.costliest` | most-negative `totalPnlImpact` mistake | count ≥ 3 | CRITICAL if \|impact\| ≥ 2·IMPACT_SCALE else WARNING | "No Stop Loss cost you −$2,140 across 6 trades this period — your most expensive leak. {advice}" |
| `mistake.overallRate` | `overallMistakeRate` vs `prevOverallMistakeRate` moved ≥ 10pp | closedCount ≥ 8, previous≠null | WARNING if rising / POSITIVE if falling | "You tagged a mistake on 44% of trades, up from 28% last period." / "…down from 41% to 22% — you're trading cleaner." |
| `mistake.repeating` | per mistake: `count >= prevCount` and `totalImpact < 0` | count ≥ 3, previous≠null | WARNING | "You tagged Revenge Trade 5×, up from 3× — it isn't improving. {advice}" |
| `mistake.reducing` | per mistake: `prevCount ≥ 3` and `count ≤ prevCount·(1−REDUCE_FRACTION)` | previous≠null | POSITIVE | "You cut FOMO from 6× to 2× — keep that going." |
| `mistake.cleared` | per mistake: `prevCount ≥ 3` and `count == 0` | previous≠null | POSITIVE | "Zero Early Exit trades this period, down from 4×. That habit looks fixed." |
| `mistake.new` | per mistake: `prevCount == 0` and `count ≥ 2` | previous≠null | WARNING | "Large Size is new this period (3 trades, −$480). {advice}" |
| `mistake.cleanEdge` | `cleanTradeWinRate − taggedWinRate ≥ CLEAN_EDGE_GAP_PP` | clean ≥ 5 | POSITIVE | "Your clean trades win 64% vs 38% when you tag a mistake — discipline is worth ~26 points." |
| `mistake.concentration` | one mistake ≥ CONCENTRATION_PCT of `totalMistakeTrades` | totalMistakeTrades ≥ 4 | INFO (WARNING if net-negative) | "No Setup shows up in 58% of your flagged trades — fixing this one moves the needle most." |
| `mistake.doNotEnter` | per mistake: `winRate < 30` | count ≥ 4 | WARNING | "Trades where you FOMO win only 22% — treat the tag as a do-not-enter signal. {advice}" |

`{advice}` (the 8 preserved paragraphs) surfaces via detectors `costliest`, `repeating`, `new`,
`doNotEnter`.

### STRATEGY (6) — `StrategyInsightDetector`, over current/previous `StrategyWindowStats` pairs; emit `id = "<base>:<strategyId>"`, set `strategyId` + `verdict`

| id | trigger | min-sample | severity | verdict | body example |
|----|---------|-----------|----------|---------|--------------|
| `strategy.scaleUp` | top `expectancy`, `profitFactor ≥ 1.5`, `totalPnl > 0` | trades ≥ 8 | POSITIVE | SCALE_UP | "Breakout is your edge — 2.4 profit factor over 19 trades (+$3,100). Consider sizing it up." |
| `strategy.drop` | `totalPnl < 0` and `profitFactor < 1` | trades ≥ 8 | WARNING (CRITICAL if \|pnl\| ≥ 3·IMPACT_SCALE) | DROP | "Mean Reversion has lost −$1,760 across 14 trades (PF 0.6). Park it or paper-trade until it turns." |
| `strategy.declining` | `prevWinRate − curWinRate ≥ 10pp` or PnL flipped +→− | both windows ≥ 5 | WARNING | REVIEW | "Scalp slipped from 61% to 42% win rate this period — review recent entries." |
| `strategy.improving` | `curWinRate − prevWinRate ≥ 10pp` and `curPnl > 0` | both windows ≥ 5 | POSITIVE | KEEP | "Swing is trending up — 44% → 58% win rate, +$900 this period." |
| `strategy.needsData` | `0 < totalTrades < 8` | — | INFO | NEEDS_MORE_DATA | "Only 4 trades on Gap-Fill this period — need ~8 for a reliable read." |
| `strategy.steady` | `profitFactor ≥ 1.2` and `maxDrawdown ≤ 0.4·totalPnl` | trades ≥ 8 | POSITIVE | KEEP | "Trend-Follow grinds steadily — max drawdown only $420 against +$2,050 profit." |

`GetStrategyInsightsUseCase` derives `verdictByStrategyId` = verdict of each strategy's
highest-priority insight; no insight + `< 8` trades → `NEEDS_MORE_DATA`, else `KEEP`.
`headline` = first STRATEGY insight by priority.

### DIRECTION (4) — ports retired `buildLongShortInsights` + `buildInsights`

`direction.edge` (long/short win-rate gap ≥ 10pp, both sides ≥ 5, INFO),
`direction.bleeding` (one side pnl<0 while other ≥0, loser count ≥ 3, WARNING),
`trend.edge` (best−worst trend win-rate ≥ 10pp, best.trades ≥ 5, INFO),
`trend.losing` (a `TrendStat` pnl<0 or winRate<45, trades ≥ 3, WARNING).

### TIMING / RISK / DISCIPLINE (5)

`timing.bestDay` (bestDay pnl>0 ≥ IMPACT_SCALE, ≥3 trades, POSITIVE),
`timing.worstDay` (a day pnl<0, \|pnl\| ≥ IMPACT_SCALE, ≥3 trades, WARNING),
`risk.profitFactor` (`overview.profitFactor` < 1.0, closedCount ≥ 10, CRITICAL),
`risk.drawdown` (`maxDrawdown.amount ≥ max(IMPACT_SCALE, 0.5·|netPnl|)`, closedCount ≥ 10, WARNING),
`discipline.lossStreak` (`currentStreak ≤ −3`, WARNING).

**Roles in v1 (light):** never gate a detector on/off. `roles` add `deltaBoost 0.05` and slightly
sharper phrasing when relevant (`"Scalping"` → boost `timing.*` / `discipline.lossStreak`;
`"Swing"` → boost `trend.*`). Users who skipped onboarding get a stable surface.

## Context builder

```kotlin
class BuildInsightContextUseCase(
    private val getOverviewStats: GetOverviewStatsUseCase,
    private val getTradeSummary: GetTradeSummaryUseCase,
    private val getTrendPerformance: GetTrendPerformanceUseCase,
    private val getDayPerformance: GetDayPerformanceUseCase,
    private val getMistakesAnalysis: GetMistakesAnalysisUsecase,
    private val getSymbolPerformance: GetSymbolPerformanceUsecase,
) {
    operator fun invoke(
        allTrades: List<Trade>, period: TimePeriod, roles: List<String>,
        currencySymbol: String, now: LocalDate = LocalDate.now(),   // injectable for tests
    ): InsightContext
}
```

- `zone = ZoneId.systemDefault()`, `nowMs = now.plusDays(1).atStartOfDay(zone)…` (today inclusive).
- `period == ALL` → `current` over all trades, `previous = null`.
- else `curStart = now.minus(dur)`, `prevStart = now.minus(dur).minus(dur)`;
  `currentTrades = allTrades.filter { tradeDate in curStartMs until nowMs }`,
  `previousTrades = allTrades.filter { tradeDate in prevStartMs until curStartMs }`.
- `previous = null` when `previousTrades.size < MIN_PREV_WINDOW_TRADES` **or**
  `allTrades.minOf { tradeDate } > prevStartMs` (no history that far back = absence, not
  improvement) → disables all movement detectors.
- `buildWindowStats` runs the 6 injected sync use cases on the slice + `TradeMath` scalars.
  `mistakes` keeps `GetMistakesAnalysisUsecase` semantics so the Mistakes-tab strip and the
  detectors agree.
- `mistakeComparisons` / `overallMistakeRate` computed here from current vs previous
  `MistakesAnalysisData` + closed counts.

Analytics slices **both** windows from the uncapped `allTrades`, so the `GetTradesUseCase`
limit=100 bug does not affect insights.

## Performance & compute strategy

The engine is cheap: every step is an O(n)/O(n log n) pass over an in-memory `List<Trade>`
(≤ 5000, realistically 1–2k lifetime). One run ≈ 6 grouping/summing passes × 2 windows +
`TradeMath` scalars + ~24 detectors that each read 2–3 numbers — low single-digit milliseconds
on a normal device. It does **not** warrant a WorkManager job, service, or periodic
precompute-and-cache for *displaying* insights; that only pays off for closed-app nudges (see
below). Decisions:

1. **Compute reactively on a background dispatcher — no worker.** The insight flow is
   `.flowOn(Dispatchers.Default)`, split off `_selectedTabIndex` (tab swipes don't recompute),
   and recomputes only when the time filter changes or the trades Flow emits new content. Apply
   `distinctUntilChanged()` to the windowed + all-time trade flows before `combine`, so an
   idempotent Room re-emit (same rows) is a no-op. Optional: a small LRU keyed by
   `(filter, tradesHash)` if profiling shows filter-toggle churn — start without it.
2. **Keep the engine Android-free and Worker-ready.** `InsightEngine.run(context)` /
   `runStrategy(context)` and `BuildInsightContextUseCase` take plain data and return plain
   data — no `Context`, no coroutines, no Firebase. So the same code can later run inside the
   existing sync `Worker` to power notifications without refactoring.
3. **Instrument the run with a Firebase Performance trace.** Wrap the
   `buildInsightContext(...) + insightEngine.run(...)` call in the project's `withTrace(...)`
   helper (`insights_analytics` / `insights_strategy`) so real p50/p95 from devices is
   observable instead of assumed. Also emit the `insight_shown` events already in the plan.
4. **Fix `TradeRepositoryImpl.getRecentTrades` to use the indexed DAO query.** It currently
   loads the entire `trades` table and filters/limits in memory (the DAO already has
   `getRecentTrades(userId, fromMillis, limit)` with a `WHERE tradeDate >= :fromMillis LIMIT`
   query that is unused). This Room read — not the arithmetic — is the dominant cost, and it
   already slows Home / Analytics / Strategy. Switch to the indexed query and honour the
   `limit` arg (also closes the `GetTradesUseCase` limit=100 gap). Isolated change, parity
   test first; benefits the whole app, not just insights.

**When a background worker becomes worth it:** only for notifying the user while the app is
closed ("you're on a 4-trade losing streak", "FOMO is creeping back"). That path piggybacks on
the existing `SyncScheduler` `Worker`: after a sync completes, run the same pure engine, diff
against the last persisted `InsightSnapshot`, fire a notification. It aligns with the
"notifications & nudges" roadmap pillar and is out of scope here — the point 2 purity
constraint is what keeps that door open.

## Wiring

### Prefs (one-liners)
- `AccountPrefs`: add `val roles: List<String> = emptyList()`.
- `UserRepositoryImpl.getAccountPrefs`: `roles = (roles ?: emptyList<Any?>()).filterIsInstance<String>()`
  into the `AccountPrefs(...)` call (list already read).

### Analytics
- `AnalyticsUiState.Success`: add `insightsByCategory: Map<InsightCategory, List<Insight>> = emptyMap()`
  and `mistakeComparisons: List<MistakeComparison> = emptyList()`.
- `AnalyticsViewModel`: inject `userRepository`, `buildInsightContext`, `insightEngine`,
  `currencyPreferences` (or a symbol use case), `analyticsManager` — all Koin-resolvable, so
  `viewModelOf(::AnalyticsViewModel)` is unchanged. Restructure the flow:
  - `personalizationFlow(userId): Flow<Personalization(roles, symbol)>` — a `flow { emit(...) }`
    that reads `getAccountPrefs` once, `.onStart { emit(empty) }.catch { emit(default) }`.
  - Split a **tab-independent** `analyticsData` flow: `_selectedFilter.flatMapLatest { combine(
    windowTrades.distinctUntilChanged(), allTrades.distinctUntilChanged(), personalizationFlow) {
    ... withTrace("insights_analytics") { buildInsightContext(...); insightEngine.run(ctx) } ... } }.flowOn(Dispatchers.Default)`
    (`combine` stays at 3 inner args). Then
    `uiState = combine(analyticsData, _selectedTabIndex) { d, tab -> d.toSuccess(tab) }` —
    tab switches no longer recompute stats/insights (removes existing waste); idempotent Room
    re-emits are dropped by `distinctUntilChanged`.
  - Instrumentation: `MutableSet<String> loggedImpressions`; on `Success`, for each insight in
    the categories visible on the current tab, `if (loggedImpressions.add(id))
    analyticsManager.logEvent("insight_shown", mapOf("id" to id, "category" to category.name,
    "severity" to severity.name))`. Clear on `onSelectFilter`.
- New `presentation/components/InsightsCard.kt` — `InsightsCard(title, insights, modifier,
  icon = Icons.Default.Lightbulb, maxItems = Int.MAX_VALUE)`; container `RoundedCornerShape(16.dp)`
  + `surfaceVariant` + 16.dp padding; icon-chip header + `HorizontalDivider`; per item: 6.dp
  bullet colored `when(severity){CRITICAL,WARNING->DangerRed; POSITIVE->SuccessGreen; INFO->PrimaryBlue}`
  + `Text(body, 13.sp, lineHeight 19.sp)` + optional chip row + divider. Renders nothing when empty.
  Replaces the 3 duplicated render blocks.
- `MistakesTab(data, insights, mistakeComparisons, currencySymbol)` — **redesign**:
  - keep `MistakeSummaryStrip`.
  - `CostRankedMistakesCard` — keep the existing `MistakeRow` bar visuals from
    `data.topMistakes.sortedBy { totalPnlImpact }`, add a per-row delta pill
    (`▲2 / ▼3 / •new / •cleared`) from `mistakeComparisons`.
  - `RepeatingReducingSection(insights)` — "Still costing you" (`mistake.repeating*` /
    `mistake.new*`) vs "Improving" (`mistake.reducing*` / `mistake.cleared*` /
    `mistake.overallRate` when falling).
  - `InsightsCard("Coaching", <per-mistake advice insights>)`.
  - reuse the green `DisciplineBanner` visual for `mistake.cleanEdge` (fallback to
    `data.cleanTradeWinRate`).
  - **delete `MistakeSuggestions.kt`** (advice strings → `InsightCopy.mistakeAdvice`).
- `TrendTab(data, insights)` — delete private `Insight`, `buildInsights`, `TrendInsightsCard`;
  replace section 5 with `InsightsCard("Trading Insights", insights)`. Charts stay.
- `OverView(..., insights)` — replace `LongShortInsightsCard(summary)` (line 39) with
  `InsightsCard("Insights", <DIRECTION+TIMING+RISK+DISCIPLINE insights by priority>)`.
  **Delete `LongShortInsightsCard.kt`**.
- `AnalyticsScreen.AnalyticsContent` — thread `state.insightsByCategory` /
  `state.mistakeComparisons` into the three tabs; symbol via `LocalCurrencySymbol.current`.

### Strategy
- `StrategyStatsCalculator` (new) — extract `computeStatsForStrategy` verbatim from
  `GetStrategyStatsUsecase`; repoint that use case to it (zero behavior change).
- `GetStrategyInsightsUseCase(strategyRepository, tradeRepository, insightEngine)` →
  `Flow<StrategyInsightsResult(headline, verdictByStrategyId, insightsByStrategyId)>`.
  `combine(getStrategies(), getRecentTrades(userId, prevStartMs, 1000))` — one query from the
  previous-window start (limit 1000, bypasses the 100 cap); group by `strategyId` (skip blank);
  split each into current/previous; `StrategyStatsCalculator` + `TradeMath` →
  `StrategyWindowStats` pairs → `StrategyInsightContext` → `insightEngine.runStrategy(ctx)`.
- `StrategiesUiState.Success`: add `strategyInsights: List<Insight> = emptyList()` (headline
  first) and `verdictByStrategyId: Map<String, StrategyVerdict> = emptyMap()`.
- `StrategyViewModel`: inject `getStrategyInsightsUseCase`, `userRepository`, `analyticsManager`
  (`viewModelOf` unchanged). `combine` the existing `getStrategyStatsUsecase(userId, period)`
  with `getStrategyInsightsUseCase(userId, period, roles, symbol)` (roles via the same
  `personalizationFlow` pattern); `.flowOn(Dispatchers.Default)`; log `insight_shown` once each.
  Sort changes must not recompute insights.
- `StrategyCard(..., verdict: StrategyVerdict?)` — pill next to `RankBadge`, reusing `RankPill`
  style: SCALE_UP→green "Scale up", KEEP→blue "Keep", REVIEW→orange "Review", DROP→red "Drop",
  NEEDS_MORE_DATA→grey "More data"; `null` → nothing.
- `StrategySuccessView` — pass `verdict = uiState.verdictByStrategyId[strategy.id]`; add a
  headline `item {}` above `PeriodSelector`:
  `uiState.strategyInsights.firstOrNull()?.let { InsightsCard("Strategy focus", listOf(it)) }`.
- `StrategyDetailViewModel` — repoint inline math (L69-92) to `TradeMath`
  (`profitFactor` keeps the `999.0` sentinel: `TradeMath.profitFactor(sorted) ?: if
  (TradeMath.grossProfit(sorted) > 0) 999.0 else 0.0`); add a previous-window slice for this
  strategy and reuse `GetStrategyInsightsUseCase` filtered to `strategyId` → `List<Insight>`.
  `StrategyDetailUiState.Success`: add `insights: List<Insight> = emptyList()`.
- `StrategyDetailsScreen.StrategyDetailSuccess` — `InsightsCard("Strategy insights",
  state.insights)` after `StrategyRiskRewardCard`.
- `di/ViewModelModule.kt` — the manual `viewModel { (strategyId: String) ->
  StrategyDetailViewModel(...) }` block must add the new `get()` args (runtime failure, not
  compile-time — easy to miss).

### DI — `di/InsightsModule.kt` (new)
```kotlin
val insightsModule = module {
    single { InsightEngine(analyticsDetectors, strategyDetectors, analytics = get()) }
    factory { BuildInsightContextUseCase(get(), get(), get(), get(), get(), get()) }
    factory { GetStrategyInsightsUseCase(get(), get(), get()) }
    factory { StrategyStatsCalculator() }
}
```
Register `insightsModule` in the `startKoin { modules(...) }` list in the Application class
(`TradeTrack.kt`). Repoint `GetStrategyStatsUsecase` factory in `UseCaseModule.kt` if it now
takes `StrategyStatsCalculator`.

## Shared cleanups (in scope)

- `core/util/TradeMath.kt` (new) — `grossProfit`, `grossLoss`, `profitFactor:Double?`,
  `maxDrawdown(): DrawdownResult(amount, atEpochMillis?)`, `maxDrawdownFromSeries`, `winStreak`,
  `lossStreak`, `currentStreak`, `avgWin`, `avgLoss`, `expectancy`, `winRate` (0..100, canonical
  win = `pnl > 0`). Private `closedSorted(trades)`.
- Repoint `StrategyDetailViewModel` inline math → `TradeMath` (parity test first).
- Extract `StrategyStatsCalculator` from `GetStrategyStatsUsecase`.
- **Do not** change `GetMistakesAnalysisUsecase`'s `>= 0` win rule in this work (risk) — file a
  follow-up to unify. Mistake win rates keep coming from `MistakesAnalysisData` so the tab stays
  self-consistent.
- Delete `MistakeSuggestions.kt`, `LongShortInsightsCard.kt`, and `buildInsights` /
  private `Insight` / `TrendInsightsCard` in `TrendTab.kt`. `grep -rn
  "buildMistakeSuggestions\|LongShortInsightsCard\|buildLongShortInsights\|buildInsights\|MistakeSuggestion"`
  for stragglers.
- `GetEquityCurveDataUsecase` — leave as-is (different granularity); optionally reuse
  `TradeMath.maxDrawdownFromSeries` only if trivial.
- **`TradeRepositoryImpl.getRecentTrades`** — switch from "load whole table + filter in memory"
  to the existing unused indexed DAO query (`WHERE tradeDate >= :fromMillis ... LIMIT :limit`),
  and honour the `limit` arg (also closes `GetTradesUseCase` limit=100). Parity test first;
  biggest real-world perf win, benefits Home / Analytics / Strategy too. See *Performance &
  compute strategy* point 4. Do this in Phase 1 alongside `TradeMath`.

## Testing

Add to `gradle/libs.versions.toml` + `app/build.gradle.kts` `testImplementation`:
`kotlinx-coroutines-test 1.8.1`, `mockk 1.13.11`, `turbine 1.1.0`. Delete the two
`com.example.myapplication` template test files. New tests under `app/src/test/java/com/wallstreet/`.

Helpers (`app/src/test/java/com/wallstreet/testutil/`): `MainDispatcherRule` (JUnit4 TestWatcher),
`TradeFixtures.trade(id, pnl, daysAgo, mistakes, strategyId, type, trend, symbol)` +
`winners(n)` / `losers(n, mistakes)`. `daysAgo` → deterministic `tradeDate`;
`BuildInsightContextUseCase` takes injectable `now`.

| Area | Cases |
|------|-------|
| `TradeMathTest` | profit factor with/without losses; drawdown on a known peak→trough→recovery series; win/loss/current streak; expectancy; avgWin/avgLoss; empty & all-open lists |
| per-detector (~23 files) | fires on a fixture meeting trigger+threshold; does not fire below min-sample / with `previous == null`; `body` contains the formatted figure and (coaching detectors) the exact preserved advice sentence; `severity`; `priority in 0.0..1.0` |
| `InsightEngineTest` | dedupe keeps higher priority; `minPriority` filter; `perCategoryCap` / `globalCap`; sort order; a throwing detector is swallowed, others still run |
| `BuildInsightContextUseCaseTest` | trades land in correct window by `daysAgo`; `previous == null` below `MIN_PREV_WINDOW_TRADES`; `previous == null` when earliest trade newer than `prevStart`; `ALL` ⇒ `previous == null`; `mistakeComparisons` / `overallMistakeRate` correct; roles/symbol pass through |
| `GetStrategyInsightsUseCaseTest` | fake repos via `flowOf`; verdict map + headline; blank-`strategyId` excluded; `NEEDS_MORE_DATA` default |
| `AnalyticsViewModelTest` (Turbine+MockK) | `Loading → Success`; `insightsByCategory` populated; roles-fetch failure ⇒ empty roles, still `Success`; `onSelectFilter` re-emits; tab switch does **not** recompute (`buildInsightContext` called once per filter) |
| `StrategyViewModelTest` (Turbine+MockK) | `Success` carries `strategyInsights` + `verdictByStrategyId`; `onPeriodSelected` re-emits; sort change doesn't change insights |

## Risks

- **`GetTradesUseCase` limit=100.** Analytics is immune (windows sliced from uncapped
  `allTrades`). The Phase-1 `getRecentTrades` fix (Shared cleanups / Performance point 4) closes
  this properly; until then `GetStrategyInsightsUseCase` queries the repo directly at limit 1000
  and `StrategyDetailViewModel`'s capped `getTradesUsecase(userId, period, 500)` path is
  acceptable because per-strategy counts are small.
- **`strategy` free-text vs `strategyId`.** Detectors group by `strategyId`, skip blank — matches
  `GetStrategyStatsUsecase`. Trades with only free-text strategy are invisible to strategy
  insights (acceptable v1).
- **Win-rate divergence.** Engine standardizes on `pnl > 0` via `TradeMath`; mistake win rates
  stay on `MistakesAnalysisData` (`>= 0`) for tab consistency. Overview/Trend numbers may differ
  by a hair from the old cards — now consistent with `AnalyticsRepositoryImp`.
- **Small-sample noise.** Every detector has a min-sample gate + confidence term;
  `previous == null` disables movement detectors; `perCategoryCap` keeps cards short; verdict
  hysteresis via wide thresholds + `NEEDS_MORE_DATA`.
- **Recompute cost.** ~few ms per run (6 sync use cases × 2 windows + engine). Mitigated by
  `.flowOn(Dispatchers.Default)`, splitting insights off `_selectedTabIndex`,
  `distinctUntilChanged` on the trade flows, single `allTrades` source (no new Firestore reads).
  The dominant cost is the Room read, addressed by the Phase-1 `getRecentTrades` fix. A
  `withTrace` around the run gives real device numbers. See *Performance & compute strategy*.
- **`WhileSubscribed(5000)` re-subscription** rebuilds the `personalizationFlow` Firestore read
  when returning after >5s; `.onStart { emit(empty) }` keeps UI unblocked. Consider `shareIn`
  if profiling shows churn.
- **`ViewModelModule` manual `StrategyDetailViewModel` block** — grows a constructor arg,
  runtime failure not compile-time.

## Build sequence

- **Phase 0** — add test deps; delete `com.example.myapplication` tests; add `MainDispatcherRule`
  + `TradeFixtures`.
- **Phase 1** — `TradeMath.kt` + `TradeMathTest`; repoint `StrategyDetailViewModel` inline math
  (parity tests); extract `StrategyStatsCalculator`, repoint `GetStrategyStatsUsecase`; fix
  `TradeRepositoryImpl.getRecentTrades` to use the indexed DAO query + honour `limit` (parity
  test — same rows out for existing callers).
- **Phase 2** — engine core types + `PriorityScore` + `InsightCopy` (+ moved `curatedAdvice`) +
  `InsightThresholds`; `InsightEngineTest`.
- **Phase 3** — detectors (catalog above) + per-detector tests; `BuildInsightContextUseCase` +
  tests.
- **Phase 4** — `insightsModule` + register in `TradeTrack.kt`; `AccountPrefs.roles` +
  `UserRepositoryImpl` one-liner.
- **Phase 5** — `InsightsCard`; `AnalyticsUiState` fields; `AnalyticsViewModel` refactor;
  redesign `MistakesTab`; swap `TrendTab` / `OverView` blocks; thread `AnalyticsScreen`; delete
  the 3 retired generators; `AnalyticsViewModelTest`.
- **Phase 6** — `GetStrategyInsightsUseCase` + tests; `StrategiesUiState` fields;
  `StrategyViewModel` inject; `StrategyCard` verdict badge; `StrategySuccessView` headline;
  `StrategyDetailViewModel` / `StrategyDetailUiState` / `StrategyDetailsScreen` per-strategy card;
  update `ViewModelModule` manual block; `StrategyViewModelTest`.
- **Phase 7** — instrumentation (`insight_shown` / `insight_impression` in both VMs); empty-state
  checks; strip dead imports; `grep` for retired symbols.

## Verification

- `./gradlew :app:compileDebugKotlin` after each phase.
- `./gradlew :app:testDebugUnitTest` — engine, detectors, context builder, `TradeMath`, both
  ViewModels green.
- Manual on device/emulator (test account `nirajpardeshi2002@gmail.com` / `Niraj@123`):
  - Analytics → Mistakes tab: cost-ranked mistakes with ▲/▼/new/cleared pills; "Still costing
    you" vs "Improving" sections; coaching lines carry the real numbers + preserved advice; clean
    vs tagged edge banner.
  - Change the time filter → insights recompute; switch tabs → no recompute flicker.
  - Analytics → Overview / Trend: `InsightsCard` replaces the old blocks, no duplicates.
  - Strategy list: verdict badge per card (Scale up / Keep / Review / Drop / More data);
    "Strategy focus" headline. Strategy detail: per-strategy insights card below risk/reward.
  - New account (no history): no movement insights, no crashes, empty states intact.
- Firebase Analytics DebugView: `insight_shown` events with `{id, category, severity}`.
- Firebase Performance: `insights_analytics` / `insights_strategy` traces present; p50 within a
  few ms, p95 not pathological on a mid-tier device with a large trade history.
- Rapidly toggle the time filter and swipe tabs: no main-thread jank; tab swipes issue no
  recompute (log/trace count unchanged).
