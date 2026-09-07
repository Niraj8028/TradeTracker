# Discipline Checklist — Design

**Date:** 2026-09-07
**Status:** Draft for review
**Branch:** `docs/discipline-system-planning`
**Related:** [`2026-09-07-discipline-system-roadmap.md`](./2026-09-07-discipline-system-roadmap.md) — this feature is Pillar 1 of that roadmap.

---

## 1. Summary

A daily **Pre-market + Post-market checklist** of yes/no items the trader completes each
trading day. It is surfaced as a **"Today's Checklist" card on the Dashboard** that opens a
dedicated **Checklist screen**. Each day's results roll up into a **blended discipline score**
plotted over the last 30 days, with current and longest streak.

The feature follows TradeTrack's existing architecture: offline-first Room storage, Firestore
sync via a WorkManager worker, Koin DI, Navigation 3 routes, and a `StateFlow<UiState>`
ViewModel.

An **AI insights panel** is part of the intended end state but is **not built in v1**. The
design leaves a clean seam (`DisciplineInsightProvider` + a hidden UI slot) so it can be added
later without touching the rest of the feature.

## 2. Goals / Non-goals

### Goals
- Give traders a repeatable pre-market and post-market routine they can tick off in seconds.
- Let each trader edit the checklist to match how they actually trade.
- Turn daily completion into a single **discipline score** and show its 30-day trend.
- Work fully offline; sync across devices when a connection is available.
- Reuse existing trade / mistake data rather than asking the user to re-enter anything.

### Non-goals (v1)
- AI-generated insights text (designed-for, not built — see §11).
- Reminders / push notifications (Pillar 2 of the roadmap).
- Typed or numeric checklist items — every item is boolean in v1.
- Attaching a checklist to an individual trade.
- Configurable score weights in the UI (weights are constants, RemoteConfig-overridable).

## 3. Decisions locked during brainstorming

| Question | Decision |
|---|---|
| v1 scope | Full: pre + post sections, editable items, completion %, 30-day chart. AI panel planned, not built. |
| Placement | "Today's Checklist" card on the Dashboard → opens a Checklist push screen. |
| Storage / sync | Room + Firestore sync, mirroring the trade sync path. |
| Item type | Boolean only. |
| Discipline score | Blended: checklist completion + mistake-free trades + stop-loss adherence. |

## 4. User-facing behaviour

### 4.1 Dashboard card (`DisciplineChecklistCard`)
- Shown on the Home dashboard (`presentation/home/HomeScreen.kt`).
- Content: title "Today's checklist", progress (`"4 / 13 done"`) with a slim progress bar, a
  streak chip (`"🔥 6-day streak"` when `currentStreak > 0`), and a subtle prompt that changes
  by time of day — "Pre-market prep" before market open (09:15 IST), "Post-market review" after
  close (15:30 IST).
- Tap anywhere on the card → navigate to `AppRoute.Home.ChecklistRoute`.
- Empty/first-run state: "Set up your trading routine" → opens the screen seeded with defaults.

### 4.2 Checklist screen (`ChecklistScreen`)
Sections top to bottom:

1. **Header** — today's date, a circular progress indicator for overall completion, current
   streak.
2. **Pre-market** — list of enabled `PRE_MARKET` items as tappable rows with a checkbox.
   Tapping toggles immediately (optimistic write).
3. **Post-market** — same for `POST_MARKET` items.
4. **Discipline** section — 30-day chart of the daily score (Vico), current + longest streak,
   30-day average score, and a small legend explaining the three score components.
5. **AI insights slot** — renders nothing in v1 (see §11).

Overflow menu: **Edit checklist**, **Reset to default**.

### 4.3 Edit mode
- Toggled from the overflow menu; same list with editing affordances.
- Per item: rename inline, drag handle to reorder within its group, enable/disable switch,
  delete.
- "Add item" opens a bottom sheet (follow the project's bottom-sheet pattern: surface colour,
  0dp elevation, `skipPartiallyExpanded`, `navigationBarsPadding`) with a text field and a
  group selector (Pre-market / Post-market).
- **Reset to default** restores the default template. It only replaces template items; it never
  deletes historical `daily_checklist_entries`.
- Disabling an item keeps its history but removes it from today's list and from future
  completion denominators.

### 4.4 Behaviour notes
- The "day" is the local calendar date (device time zone; the app is IST-centric).
- Only **today** is editable. Past days are read-only; the 30-day chart displays them but does
  not allow back-filling in v1.
- Weekends / holidays: a day only enters the score series if there was checklist activity or at
  least one trade that day. Gaps are neutral and do not break a streak (see §9, open item 2 —
  proposed default, confirm).

## 5. Default template

Seeded on first use; fully editable afterwards. Flavoured for Indian intraday / F&O traders.

**Pre-market (7)**
1. Reviewed overnight global cues / SGX Nifty
2. Marked key support & resistance levels
3. Noted today's market bias (up / down / range)
4. Set maximum loss for the day
5. Set maximum number of trades for the day
6. Confirmed capital & margin available
7. Mentally ready — no tilt carried from yesterday

**Post-market (6)**
1. Logged every trade taken today
2. Attached charts / screenshots to each trade
3. Every trade had a pre-defined stop-loss
4. Did not average into a losing position
5. Tagged mistakes honestly
6. Wrote one lesson from today

## 6. Data model

### 6.1 Domain (`domain/model`)
```
enum class ChecklistGroup { PRE_MARKET, POST_MARKET }

data class ChecklistItem(
    val id: String,
    val group: ChecklistGroup,
    val label: String,
    val order: Int,
    val enabled: Boolean,
)

data class DailyChecklistEntry(val itemId: String, val checked: Boolean, val checkedAt: Long?)

data class DailyChecklist(
    val date: LocalDate,
    val entries: List<DailyChecklistEntry>,
)

data class DisciplineDay(
    val date: LocalDate,
    val checklistCompletion: Float,   // 0f..1f
    val mistakeFreeRatio: Float,      // 0f..1f
    val stopLossAdherence: Float,     // 0f..1f
    val score: Float,                 // 0f..1f, weighted
    val tradeCount: Int,
)

data class DisciplineSummary(
    val days: List<DisciplineDay>,    // ascending, only days with activity
    val currentStreak: Int,
    val longestStreak: Int,
    val avgScore30d: Float,
)
```

### 6.2 Room (`data/local/entity`, `data/local/dao`)

`ChecklistItemEntity` — table `checklist_items`
| column | type | notes |
|---|---|---|
| `id` | String PK | UUID |
| `userId` | String | |
| `group` | String | `ChecklistGroup` name |
| `label` | String | |
| `sortOrder` | Int | |
| `enabled` | Boolean | default true |
| `deleted` | Boolean | soft delete for sync |
| `updatedAt` | Long | |
| `syncStatus` | `SyncStatus` | reuse existing enum (`PENDING/SYNCED/FAILED`) |
| `lastSyncAttempt` | Long? | |
| `syncError` | String? | |

`DailyChecklistEntryEntity` — table `daily_checklist_entries`
| column | type | notes |
|---|---|---|
| `id` | String PK | `"${userId}_${epochDay}_${itemId}"` — deterministic, so upserts are idempotent |
| `userId` | String | |
| `epochDay` | Long | `LocalDate.toEpochDay()`; indexed for range queries |
| `itemId` | String | |
| `checked` | Boolean | |
| `checkedAt` | Long? | |
| `syncStatus` / `lastSyncAttempt` / `syncError` | | as above |

DAOs:
- `ChecklistDao` — `observeItems(userId): Flow<List<ChecklistItemEntity>>` (excludes `deleted`),
  `upsert`, `softDelete(id)`, `updateOrder(list)`, `setEnabled(id, Boolean)`,
  `getPendingSync(userId)`, `markSynced(...)`, `countForUser(userId)`.
- `DailyChecklistDao` — `observeDay(userId, epochDay): Flow<List<DailyChecklistEntryEntity>>`,
  `upsertEntry(entity)`, `observeRange(userId, fromEpochDay, toEpochDay): Flow<List<...>>`,
  `getPendingSync(userId)`, `markSynced(...)`, `deleteForUser(userId)`.

`AppDatabase`: version **2 → 3**, add `MIGRATION_2_3` creating both tables + the
`epochDay` index. Register both DAOs as `single` in `databaseModule`.

### 6.3 Firestore (`data/model`, `data/mapper`)
- `FirebaseService.Collections`: add `CHECKLIST_ITEMS = "checklist_items"`,
  `CHECKLIST_ENTRIES = "checklist_entries"`.
- `ChecklistItemDto`, `DailyChecklistEntryDto` — plain `data class` with no-arg defaults, same
  style as `TradeDto`. Documents keyed by the entity `id` (so the entry id encodes user + day +
  item and is stable).
- Mappers: `toDto()`, `toEntity(syncStatus)`, `toDomain()` in `data/mapper`, matching
  `TradeEntityMapper` / `TradeMapper` conventions.

## 7. Sync design

Mirror the trade path rather than overload `SyncWorker`.

- **Write:** repository writes to Room with `SyncStatus.PENDING`, then calls
  `SyncScheduler.scheduleChecklistSync(userId)`.
- `SyncScheduler` gains `scheduleChecklistSync(userId)` / `cancelChecklistSync(userId)` —
  unique work name `checklist_sync_<userId>`, `ExistingWorkPolicy.KEEP`, `NetworkType.CONNECTED`.
- **`ChecklistSyncWorker`** (`CoroutineWorker`, `KoinComponent`, `by inject()` — same shape as
  `SyncWorker`): pushes pending `checklist_items` then pending `checklist_entries` to Firestore,
  flips each row to `SYNCED` / `FAILED`, returns `Result.retry()` if any failed.
- **Seed:** `ChecklistRepository.seedFromFirestore(userId)` pulls remote items + a bounded
  window of entries (last ~60 days) into Room, skipping ids that are still `PENDING` locally.
  Called from `ChecklistViewModel.init` (same pattern as `HomeViewModel` seeding trades).
- Deletes propagate via the `deleted` flag on `checklist_items` (tombstone), not row removal.

## 8. Repository layer

`domain/repository/ChecklistRepository`
```
fun observeTemplate(userId: String): Flow<List<ChecklistItem>>
suspend fun upsertItem(userId: String, item: ChecklistItem)
suspend fun deleteItem(userId: String, itemId: String)          // soft delete
suspend fun reorder(userId: String, group: ChecklistGroup, orderedIds: List<String>)
suspend fun setEnabled(userId: String, itemId: String, enabled: Boolean)
suspend fun resetToDefault(userId: String)                      // template only

fun observeDay(userId: String, date: LocalDate): Flow<DailyChecklist>
suspend fun setEntry(userId: String, date: LocalDate, itemId: String, checked: Boolean)
fun observeRange(userId: String, from: LocalDate, to: LocalDate): Flow<List<DailyChecklist>>

suspend fun syncPending(userId: String): Boolean
suspend fun seedFromFirestore(userId: String)
suspend fun hasTemplate(userId: String): Boolean
```

`data/repository/ChecklistRepositoryImpl(checklistDao, dailyChecklistDao, syncScheduler, firestore)`
— structure copied from `TradeRepositoryImpl`. Bound in `repositoryModule` as
`single<ChecklistRepository>`.

Default template lives in `core/constants/DefaultChecklist.kt` (list of `ChecklistItem` with
stable ids like `"pre_global_cues"`). `resetToDefault` and first-run seeding both use it.

## 9. Domain use cases (`domain/usecase/checklist`)

Follow the existing style — small classes with `operator fun invoke`, registered as `factory`
in `useCaseModule`.

- **`GetTodayChecklistUseCase`** — combines enabled template + today's entries into a grouped
  view object with per-group and overall completion fractions.
- **`ToggleChecklistItemUseCase`** — `invoke(userId, date, itemId, checked)` → `repo.setEntry`.
- **`UpdateChecklistTemplateUseCase`** — thin wrappers for add / rename / delete / reorder /
  enable / reset (or keep as separate one-line use cases if that matches the codebase better —
  decide during implementation, matching how `strategy` use cases are split).
- **`ComputeDisciplineScoreUseCase`** — **pure function**, no I/O. The core of the feature.

  For each date in the range, given that day's `DailyChecklist`, that day's enabled template
  size, and that day's `List<Trade>`:

  ```
  checklistCompletion = checkedEnabledItems / enabledItemCount           // 0 if enabledItemCount == 0
  mistakeFreeRatio    = trades.count { it.mistakes.isEmpty() } / trades.size    // 1f if trades.isEmpty()
  stopLossAdherence   = 1f - (trades.count { it.mistakes.hasNoStopLoss() } / trades.size)  // 1f if trades.isEmpty()

  score = 0.5f * checklistCompletion
        + 0.3f * mistakeFreeRatio
        + 0.2f * stopLossAdherence
  ```

  Weights are `object DisciplineWeights` constants, documented as RemoteConfig-overridable
  later (aligns with the existing `RemoteConfigManager` pattern). `hasNoStopLoss()` matches the
  trade against the project's existing mistake-tag constant(s) for "no stop-loss" — see §12,
  open item 1.

- **`GetDisciplineSummaryUseCase`** — `invoke(userId): Flow<DisciplineSummary>`. Combines
  `repo.observeRange(userId, today-29, today)` with `GetTradesUseCase(userId, last 30 days)` and
  `observeTemplate`, maps through `ComputeDisciplineScoreUseCase`, then:
  - includes a day in `days` only if it had checklist activity **or** `tradeCount > 0`;
  - `currentStreak` / `longestStreak` = consecutive included days with `score >= 0.8f`
    (threshold constant — §12, open item 3);
  - `avgScore30d` = mean `score` over included days (0 if none).

## 10. Presentation (`presentation/checklist`)

- **`ChecklistUiState`** — `sealed class` with `Loading` / `Error(message)` / `Success(...)`.
  `Success` carries: `preItems` + `postItems` (each item with `checked`), `overallCompletion`,
  `currentStreak`, `editMode: Boolean`, and `summary: DisciplineSummary`.
- **`ChecklistViewModel`** (`viewModelOf(::ChecklistViewModel)`):
  - `init`: `seedFromFirestore(userId)`, and if `!hasTemplate(userId)` → `resetToDefault`.
  - state pipeline:
    ```
    combine(
        getTodayChecklistUseCase(userId),
        getDisciplineSummaryUseCase(userId),
        editMode,
    ) { today, summary, edit -> Success(...) }
      .flowOn(Dispatchers.Default)
      .catch { emit(Error(it.message ?: "…")) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Loading)
    ```
  - intents: `toggle(itemId)`, `enterEdit()` / `exitEdit()`, `addItem(group, label)`,
    `rename(itemId, label)`, `remove(itemId)`, `reorder(group, orderedIds)`,
    `setEnabled(itemId, Boolean)`, `resetToDefault()`. Each launches in `viewModelScope` and
    delegates to a use case.
- **`ChecklistScreen.kt`** — hosted in the `Home` nav graph via `MainScaffold`; respect the
  MainScaffold inset / padding rules (screen content owns its padding; do not double-apply
  scaffold insets). Uses the shared tab / card components where they fit.
- **Components** (`presentation/checklist/components`): `ChecklistItemRow`,
  `ChecklistGroupSection`, `DisciplineScoreChart` (Vico line chart, endpoint emphasised, faint
  grid, theme-token colours), `DisciplineStatStrip` (streak / longest / avg), `AddItemSheet`,
  `DisciplineInsightsCard` (see §11).
- **`presentation/home/components/DisciplineChecklistCard.kt`** — added to `HomeScreen`'s
  column. Reads a lightweight slice of state; simplest approach is a tiny
  `getTodayChecklistUseCase` collection inside `HomeViewModel`, or a dedicated
  `ChecklistSummaryViewModel` scoped to the card. Decide during implementation to avoid bloating
  `HomeViewModel` (memory: HomeViewModel is already large).

## 11. AI insights — planned, not built

- `DisciplineInsightsCard` is added to the Checklist screen now but renders `null` (or is
  `hidden`) in v1, gated by `RemoteConfigManager` key `discipline_ai_insights_enabled`
  (default `false`).
- Define the seam interface so v1 code compiles against it:
  ```
  interface DisciplineInsightProvider {
      suspend fun summarise(input: DisciplineInsightInput): Result<DisciplineInsightText>
  }
  ```
  `DisciplineInsightInput` = the same structured data the chart already computes
  (`DisciplineSummary` + per-mistake counts + streak facts). No implementation is bound in DI
  in v1.
- When the AI approach is chosen (on-device heuristic vs Firebase Cloud Function proxy to an
  LLM — deferred decision), only a provider implementation + DI binding + un-hiding the card is
  needed. No change to data, sync, or the rest of the UI.

## 12. Open decisions (proposed defaults — confirm before implementation)

1. **"No stop-loss" mistake tag** — `ComputeDisciplineScoreUseCase.hasNoStopLoss()` needs the
   exact string(s) the mistake tracker writes into `Trade.mistakes` today. Proposal: read them
   from the existing mistake constants / RemoteConfig mistakes list; treat a configurable set
   (`{"Ignoring Stop-Loss", "No Stop-Loss"}`) as the match. **Action: confirm the real values in
   code during implementation.**
2. **Non-trading days** — proposal: a day enters the score series only if it had checklist
   activity or ≥1 trade; gaps are neutral and do not break a streak. Alternative: every calendar
   day counts and a missed day scores 0. Proposal favours habit-building over punishment.
3. **"Disciplined day" threshold** — `score >= 0.8` for streak counting. Constant.
4. **Score weights** — `0.5 / 0.3 / 0.2` for checklist / mistake-free / stop-loss. Constants,
   RemoteConfig-overridable.
5. **Past-day editing** — v1 keeps past days read-only; only today is editable.
6. **Card ownership** — whether the Dashboard card reads from `HomeViewModel` or its own small
   ViewModel (lean toward its own, given `HomeViewModel` size).

## 13. Error handling & edge cases

| Case | Behaviour |
|---|---|
| Toggle while offline | Optimistic Room write; `scheduleChecklistSync` enqueues; syncs when online. |
| Sync failure | Row marked `FAILED`, `Result.retry()`, silent to user. |
| No trades yet | `mistakeFreeRatio` / `stopLossAdherence` default to `1f`; chart still shows checklist-driven score. Empty-history copy on the chart until ≥1 day of activity. |
| User deletes all items | `hasTemplate` false on next open → **not** auto-reseeded (a `ChecklistPreferences.seeded(uid)` DataStore flag prevents re-seed); show the "Set up your routine" empty state with a "Restore default checklist" button. |
| Migration 2→3 fails | Standard Room failure; covered by an instrumented migration test. |
| Item disabled mid-day after being checked | History retained; excluded from today's denominator and future scores. |
| Clock/timezone change | Day = `LocalDate.now()` at read time; entries keyed by `epochDay`. Acceptable for v1. |

## 14. Testing strategy

Project currently has no meaningful test suite, so this feature also seeds the testing pattern.

- **JVM unit tests (highest value):**
  - `ComputeDisciplineScoreUseCase` — weight maths, empty-trades defaults, all-checked / none-checked, disabled items excluded, no-stop-loss detection.
  - `GetDisciplineSummaryUseCase` — streak / longest-streak logic, day-inclusion rule, 30-day average, threshold boundary.
  - `GetTodayChecklistUseCase` — grouping, completion fractions, disabled filtering.
- **Instrumented:**
  - `MIGRATION_2_3` — schema created, existing trade data intact.
  - `ChecklistDao` / `DailyChecklistDao` — upsert idempotency (deterministic entry id), range query bounds, soft-delete excluded from `observeItems`.
- **Repository:**
  - offline write → row `PENDING` + `scheduleChecklistSync` called;
  - `syncPending` flips `PENDING` → `SYNCED`, failure → `FAILED`;
  - `seedFromFirestore` skips locally-`PENDING` ids.
- Add **Turbine** to `libs.versions.toml` for ViewModel state-emission tests (currently absent).

## 15. Rollout

- Ship behind no user-facing flag (the feature is self-contained and additive), except the AI
  card which stays hidden via `discipline_ai_insights_enabled = false`.
- Order of implementation: migration + entities + DAOs → mappers + Firestore DTOs → repository +
  sync worker → default template + seeding → use cases (+ their unit tests) → ViewModel →
  Checklist screen → Dashboard card → nav wiring.
- No data backfill needed; existing users start with an empty history and a seeded template on
  first open of the screen.

## 16. Touched files (reference)

New: `domain/model/Checklist*.kt`, `domain/model/Discipline*.kt`,
`domain/repository/ChecklistRepository.kt`, `data/repository/ChecklistRepositoryImpl.kt`,
`data/local/entity/ChecklistItemEntity.kt`, `data/local/entity/DailyChecklistEntryEntity.kt`,
`data/local/dao/ChecklistDao.kt`, `data/local/dao/DailyChecklistDao.kt`,
`data/model/Checklist*Dto.kt`, `data/mapper/Checklist*Mapper.kt`,
`data/sync/ChecklistSyncWorker.kt`, `core/constants/DefaultChecklist.kt`,
`core/preferences/ChecklistPreferences.kt`, `domain/usecase/checklist/*`,
`presentation/checklist/*`, `presentation/home/components/DisciplineChecklistCard.kt`.

Modified: `data/local/database/AppDatabase.kt` (v3 + `MIGRATION_2_3`), `data/sync/SyncScheduler.kt`,
`data/remote/FirebaseService.kt` (collections), `di/DatabaseModule.kt`, `di/RepositoryModule.kt`,
`di/UseCaseModule.kt`, `di/ViewModelModule.kt`, `navigation/Routes.kt`,
`navigation/HomeNavigation.kt`, `presentation/home/HomeScreen.kt` (+ possibly `HomeViewModel.kt`).
