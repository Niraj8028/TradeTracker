# AGENTS.md

This file provides guidance to AI coding agents (Claude Code, Cursor, Copilot, Gemini CLI, etc.)
when working with code in this repository. It is the shared project context: architecture,
conventions, and commands that require reading several files to piece together.

## Project shape

TradeTrack is a single-module Android app (`:app`) for logging and analysing trades. Jetpack
Compose UI, offline-first local storage, Firebase backend.

- **Language / build:** Kotlin `2.0.21`, AGP `8.9.1`, Gradle `8.11.1`, JDK `17`.
- **SDK:** `compileSdk`/`targetSdk` 36, `minSdk` 26.
- **Package / namespace / applicationId:** all `com.wallstreet` (legacy name — the app, theme
  `WallStreet`, `Application` class `TradeTrack`, and Room db `tradetrack.db` are inconsistently
  named; treat `com.wallstreet` as the canonical package). `settings.gradle.kts` still calls the
  root project "My Application".
- **Dependencies** are declared in the version catalog `gradle/libs.versions.toml`; add libraries
  there, not as inline coordinates (a few inline `implementation("…")` lines exist in
  `app/build.gradle.kts` — do not add more).
- **`app/google-services.json` is required and git-ignored.** A fresh clone will not compile until
  it is added.

## Commands

```bash
./gradlew :app:assembleDebug                 # build debug APK
./gradlew :app:installDebug                  # build + install on a connected device/emulator
./gradlew :app:testDebugUnitTest             # JVM unit tests
./gradlew :app:testDebugUnitTest --tests "com.wallstreet.Foo.bar"   # single test / class
./gradlew :app:connectedDebugAndroidTest     # instrumented tests (device required)
./gradlew :app:lintDebug                     # Android Lint
./gradlew clean
```

No Detekt / ktlint / Spotless is configured — `lintDebug` is the only static check. `kotlin.code.style=official`.

## Architecture

### Layering (packages under `com.wallstreet`)

`presentation` → `domain` → `data`, plus `core` (cross-cutting) and `di` / `navigation`.

- **`domain/`** — pure Kotlin. `model/` data classes, `repository/` interfaces, `usecase/`
  classes. No Android / Firebase imports.
- **`data/`** — `repository/` implementations of the domain interfaces, `local/` (Room), `remote/`
  + `store/` (Firestore), `mapper/` (DTO ⇄ Entity ⇄ domain), `sync/` (WorkManager), `analytics/`.
- **`presentation/`** — one package per feature (`home`, `auth`, `analytics`, `strategy`,
  `log_trade`, `onboarding`, `profile`), each with a `*Screen` composable, `*ViewModel`,
  `*UiState`, and a `components/` subpackage.
- **`core/`** — `result/` (see below), `preferences/` (DataStore wrappers), `util/`, `logging/`,
  `splash/`, `constants/`.

### Dependency injection — Koin (not Hilt)

Modules live in `di/` and are registered in the `TradeTrack` `Application` class:
`appModule`, `firebaseModule`, `databaseModule`, `repositoryModule`, `useCaseModule`,
`viewModelModule`.

- Repositories & singletons: `single<Interface> { Impl(get(), …) }`.
- Use cases: `factory { SomeUseCase(get()) }`.
- ViewModels: `viewModelOf(::SomeViewModel)`, or `viewModel { (arg: String) -> … }` for
  assisted args (see `StrategyDetailViewModel`). Injected into composables with `koinViewModel()`.
- Workers use `KoinComponent` + `by inject()` (no Koin WorkManager factory).

When you add a repository/use case/ViewModel you must also wire it into the matching module.

### Navigation — Navigation 3 (`androidx.navigation3`), not Nav-Compose

- Routes are `@Serializable` objects/classes implementing `NavKey`, nested inside the
  `AppRoute` sealed interface (`navigation/Routes.kt`).
- Two levels:
  - `AppNavigation` — root gate between `AppRoute.OnBoarding` and `AppRoute.Home`, driven by a
    `StartDestination` resolved at launch.
  - `HomeNavigation` — owns the bottom-bar `Scaffold` (`AppBottomBar`) plus the pushed detail
    screens; `OnboardingNavigation` — the auth/onboarding stack.
- Each level builds its own back stack with `rememberNavBackStack(SavedStateConfiguration { … })`,
  where the `SerializersModule` must register **every** `NavKey` subclass polymorphically — add a
  new screen means adding its `subclass(...)` line or it will crash on state restore.
- `NavDisplay` is configured with `rememberSaveableStateHolderNavEntryDecorator()` +
  `rememberViewModelStoreNavEntryDecorator()` so each entry gets its own ViewModelStore.
- Tab transition direction is tracked with a `MutableState<Boolean>` (slide left/right), not
  inferred from nav state.

### State management

ViewModels expose a single `StateFlow<XxxUiState>` where `XxxUiState` is a sealed class with
`Loading` / `Error(message)` / `Success(...)` (see `HomeUiState`). Typical pipeline:

```
inputFlow
  .flatMapLatest { … combine(useCaseFlows) { … Success(...) } }
  .flowOn(Dispatchers.Default)          // analytics maths off the main thread
  .catch { emit(Error(it.message)) }
  .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Loading)
```

Analytics use cases (`domain/usecase/home`, `domain/usecase/analytics`) are mostly **pure
functions over `List<Trade>`** — computation, not I/O.

### Result type

`core.result.Result<T>` is a **custom** sealed class (`Success` / `Error(message, cause)` /
`Loading`) — not `kotlin.Result`. Suspend repository operations that can fail return it;
observation APIs return `Flow<List<T>>` directly. `core.result.AuthState` also exists but is
only partly adopted.

### Data flow — offline-first for trades

Room is the source of truth for the trade list; Firestore is the sync target.

1. **Write:** `TradeRepositoryImpl.addTrade` inserts a `TradeEntity` with
   `SyncStatus.PENDING`, then `SyncScheduler.scheduleSync(userId)` enqueues a unique
   (`trade_sync_<userId>`, `ExistingWorkPolicy.KEEP`, network-constrained) `SyncWorker`.
2. **Sync:** `SyncWorker` → `syncPendingTrades` pushes each pending row to
   `firestore.collection(TRADES)` and flips it to `SYNCED` / `FAILED`; returns `Result.retry()`
   if any failed.
3. **Read:** `getAllTrades` observes Room via a `Flow`. `seedFromFirestore(userId)` (called from
   `HomeViewModel.init`) back-fills Room from Firestore, skipping ids that are still pending
   locally.
4. `TradeStore` is a **separate** direct Firestore `addSnapshotListener` exposed as a `StateFlow`
   (real-time, no Room) — used where live remote data is needed (e.g. `MainActivity`). Don't
   confuse the two paths.

Mappers in `data/mapper` convert `TradeDto` (Firestore) ⇄ `TradeEntity` (Room) ⇄ `Trade`
(domain) with `toDto()` / `toEntity()` / `toDomain()`. Room schema changes require a new
`Migration` in `AppDatabase` (currently `version = 2`, `MIGRATION_1_2`) — added to the builder in
`databaseModule`.

### Auth & launch routing

`MainActivity.decideStartDestination()` runs before the splash is dismissed and pushes a
`StartDestination` into `SplashGate` (a `StateFlow` the splash screen keeps-on-screen condition
reads):

- no Firebase user → `Auth`
- user with unverified email → `Otp(email)`
- `OnboardingPreferences.isOnboardingCompleted(uid)` false → `Onboarding`
- otherwise → `Home`

Onboarding completion is stored **per-uid** in DataStore. Auth is Firebase Auth + Google
Sign-In (`play-services-auth`). Logout paths flow back up through `AppNavigation` via the
`isLogoutFlow` / `goToOtp` flags — read the comments there before touching that file.

### Cross-cutting

- **Preferences:** DataStore Preferences, single store `user_prefs`; wrappers
  `OnboardingPreferences`, `CurrencyPreferences`, `ThemePreferences` in `core/preferences`.
- **Logging:** Timber — `DebugTree` in debug, `CrashlyticsTree` in release. Firebase Crashlytics
  + Analytics behind `domain.analytics.AnalyticsManager` (`FirebaseAnalyticsManagerImpl`).
- **Theme:** `WallStreetAndroidTheme`; light/dark chosen from `ThemePreferences`
  (`SYSTEM` / `LIGHT` / `DARK`). Edge-to-edge enabled in `MainActivity`.
- **Charts:** Vico. **Images:** Coil. **Animation:** Lottie. **Formatting helpers:**
  `core/util` (`CurrencyUtils`, `DateUtils`, `DurationUtils`, `TradeUtils`).

## Testing

There is effectively **no test suite yet** — only the generated `ExampleUnitTest` /
`ExampleInstrumentedTest` under the stale `com.example.myapplication` package. New tests go under
`com.wallstreet`. Available deps: JUnit4, `androidx.test` + Espresso, `compose-ui-test-junit4`.
No MockK / Turbine / Robolectric — add via `libs.versions.toml` if needed. The pure use-case
functions in `domain/usecase` are the easiest high-value targets for JVM unit tests.

## Conventions

- New screen = `presentation/<feature>/` with `<Feature>Screen.kt`, `<Feature>ViewModel.kt`,
  `<Feature>UiState.kt` (sealed), `components/`; register the ViewModel in `viewModelModule` and
  the route in `Routes.kt` **and** in the relevant `SavedStateConfiguration` polymorphic block.
- New data source = domain interface in `domain/repository`, impl in `data/repository`, bound in
  `repositoryModule`, with mappers in `data/mapper`.
- Keep `domain/` free of Android/Firebase imports.
- Prefer catalog aliases (`libs.…`) for dependencies.
