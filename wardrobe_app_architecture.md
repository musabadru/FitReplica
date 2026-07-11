# Offline-First Wardrobe App — Architecture & Build Plan

Assumes: strong Kotlin/Compose fluency, so this skips "what is a ViewModel" and focuses on structure, module boundaries, and sequencing decisions.

---

## 1. Product Shape

A **local-first wardrobe OS** with two optional plug-in modules that must never leak into the core:

- **Core**: closet management, wear/condition/laundry tracking, filtering, outfit building, analytics.
- **Avatar module**: visual outfit preview on a body model (2.5D first, 3D later).
- **Metadata module**: barcode/SKU lookup (network-dependent, opt-in, off by default).

The core app must function fully with both modules absent. Treat this as a hard architectural invariant, not a suggestion — it's what keeps the offline promise honest and keeps the two hardest, riskiest pieces from destabilizing the app you'll actually ship first.

---

## 2. Module Graph

```
:app                    → composition root, nav graph, DI wiring
:core:model             → pure Kotlin data classes, enums, no Android deps
:core:database          → Room entities/DAOs/migrations
:core:datastore         → Proto/Preferences DataStore
:core:domain            → use-cases, repository interfaces
:core:designsystem      → M3 Expressive theme, tokens, shared composables
:core:common             → Result wrappers, dispatchers, utils

:feature:closet         → grid/list, filters, item detail
:feature:outfit         → outfit builder, saved outfits
:feature:history        → wear timeline, calendar
:feature:laundry        → laundry loads, condition workflow
:feature:analytics      → stats, suggestions

:avatar:api             → interfaces only (AvatarRenderer, AvatarConfig) — :app and :feature modules depend ONLY on this
:avatar:impl-2d         → Compose-based paper-doll renderer (v1)
:avatar:impl-3d         → Godot/Unity embed (v2+, swappable later)

:metadata:api           → MetadataProvider interface
:metadata:impl-noop     → default: does nothing, always present
:metadata:impl-barcode  → CameraX + ML Kit + network provider, opt-in (privacy/network gated)
:metadata:impl-caretag-ocr → CameraX + ML Kit Text Recognition, fully on-device, no network gating needed

:backup:api             → BackupManager interface — introduced at Phase 5, not scaffolded early
:backup:impl-local      → JSON/ZIP export to device storage (Phase 5)
:backup:impl-drive      → optional cloud backup (later, opt-in)
```

**Why this matters more than it looks like it does:** `:feature:closet` never imports `:avatar:impl-2d` directly — it imports `:avatar:api` and gets an implementation injected via Hilt/Koin. This means:
- You can build and ship Phase 1 with a `NoOpAvatarRenderer` stub, and swap in the real one later without touching feature code.
- If the 3D avatar turns out to be too costly (likely, see earlier caution), you can delete `:avatar:impl-3d` entirely with zero blast radius.
- Same logic applies to metadata — the barcode module is a strategy pattern, not a fork in the app.

---

## 3. Data Layer

**Design statement:** this application uses current-state persistence, not event sourcing. Room entities are the authoritative source of current state. Event tables (`WearEventEntity`, and optionally `ConditionEventEntity` later) are introduced selectively for history, analytics, and auditability — never as the sole source of truth requiring replay to reconstruct state. Any operation that updates current state and appends a corresponding event must do both inside a single Room `@Transaction`, so the two never drift out of sync.

### 3.1 Room schema

**Strong typing for IDs:** mixing up a `ClothingId` and an `OutfitId` at a call site should be a compile error, not a runtime bug. Use inline value classes:

```kotlin
@JvmInline value class ClothingId(val value: String)
@JvmInline value class OutfitId(val value: String)
@JvmInline value class LaundryLoadId(val value: String)
@JvmInline value class WearEventId(val value: String)
```

**Generation strategy:** use ULIDs rather than plain `UUID.randomUUID()` for the underlying string. Functionally both work fine in a local-only SQLite database — this isn't about collision risk. The actual benefit is that ULIDs are lexically sortable by creation time, so `ORDER BY id` on `WearEventEntity`/`ImageEntity` gives you chronological order for free, without a separate `createdAt` sort or index. Minor, but free.

Room needs a `TypeConverter` per value class (trivial — unwrap to `String`, wrap on read).

**Enums instead of raw strings** for every categorical field — `ClothingType`, `Condition`, `Status`, `SizeSystem`, `SizeCategory` — with a shared `EnumTypeConverter<T : Enum<T>>` helper so you write the boilerplate once, not five times. Strings age fine as *storage* (Room's converter still writes a string under the hood) but give zero compile-time safety as the *model* type — enums fix that without losing migration-friendliness.

**One real caution on enum converters:** the converter stores `enum.name`, which means renaming an enum constant silently breaks every existing row that has the old name persisted — Room won't catch this at compile time, and it won't throw at runtime either, it'll just fail to match and produce nulls/crashes on deserialization depending on the converter's `valueOf` handling. Treat enum constant renames as a real migration event: either never rename (add new constants, deprecate old ones instead) or write an explicit data migration that rewrites the stored strings.

```kotlin
@Entity(tableName = "clothing_items")
data class ClothingItemEntity(
    @PrimaryKey val id: ClothingId,
    val name: String,
    val type: ClothingType,
    val brand: String?,
    val colorPrimary: String,
    val colorSecondary: String?,
    val condition: Condition,      // NEW/GOOD/WORN/NEEDS_REPAIR/TORN/RETIRED
    val status: Status,            // CLEAN/DIRTY/IN_LAUNDRY
    val timesWorn: Int = 0,        // denormalized counter — see note below
    val lastWornAt: Long?,
    val addedAt: Long,
    val sku: String? = null,
    val avatarSlot: String? = null,   // maps to avatar attachment point, nullable if avatar module absent
    val purchasePrice: Double? = null,
    val purchaseDate: Long? = null,
    val purchaseLocation: String? = null,
    val notes: String? = null
)

@Entity(tableName = "wear_events")
data class WearEventEntity(
    @PrimaryKey val id: WearEventId,
    val itemId: ClothingId,
    val outfitId: OutfitId?,
    val dateTime: Long,
    val context: String?,       // "work", "gym", freeform tag
    val notes: String?
)

@Entity(tableName = "outfits")
data class OutfitEntity(
    @PrimaryKey val id: OutfitId,
    val name: String,
    val tags: List<String>,     // fine as JSON — freeform tags have no relational integrity to preserve
    val rating: Int?,
    val createdAt: Long
)

// Junction table replaces the JSON itemIds list — gives you real foreign keys,
// cascade deletes, joinable queries, and preserved ordering via `position`.
@Entity(
    tableName = "outfit_item_cross_ref",
    primaryKeys = ["outfitId", "itemId"],
    foreignKeys = [
        ForeignKey(entity = OutfitEntity::class, parentColumns = ["id"], childColumns = ["outfitId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ClothingItemEntity::class, parentColumns = ["id"], childColumns = ["itemId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class OutfitItemCrossRef(
    val outfitId: OutfitId,
    val itemId: ClothingId,
    val position: Int
)

@Entity(tableName = "laundry_loads")
data class LaundryLoadEntity(
    @PrimaryKey val id: LaundryLoadId,
    val startedAt: Long,
    val completedAt: Long?
)

@Entity(
    tableName = "laundry_load_item_cross_ref",
    primaryKeys = ["loadId", "itemId"],
    foreignKeys = [
        ForeignKey(entity = LaundryLoadEntity::class, parentColumns = ["id"], childColumns = ["loadId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ClothingItemEntity::class, parentColumns = ["id"], childColumns = ["itemId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class LaundryLoadItemCrossRef(
    val loadId: LaundryLoadId,
    val itemId: ClothingId
)

// Separated from ClothingItemEntity: an item will eventually have multiple photos,
// thumbnails, and cached compressed versions — that's a one-to-many relationship,
// not a single URI column.
@Entity(
    tableName = "images",
    foreignKeys = [ForeignKey(entity = ClothingItemEntity::class, parentColumns = ["id"], childColumns = ["itemId"], onDelete = ForeignKey.CASCADE)]
)
data class ImageEntity(
    @PrimaryKey val id: String,
    val itemId: ClothingId,
    val uri: String,
    val thumbnailUri: String,
    val isPrimary: Boolean,
    val takenAt: Long
)
```

**Why `timesWorn` stays denormalized:** deriving it live via `COUNT(*)` on `wear_events` on every closet-list render adds a join to your most-frequently-hit screen for no real benefit. Keep it as a counter updated inside the same `@Transaction` as the `WearEvent` insert — cheap to keep consistent, and it avoids penalizing the common path. Reserve on-demand derivation for the genuinely expensive analytics (average wear interval, cost-per-wear) that don't run on every scroll.

The pattern is the same everywhere current-state and event-history need to move together — write both in one transaction so they can never disagree:

```kotlin
@Dao
abstract class ClothingDao {
    @Transaction
    open suspend fun logWear(itemId: ClothingId, event: WearEventEntity) {
        insertWearEvent(event)
        updateLastWorn(itemId, event.dateTime)  // also increments timesWorn
    }

    @Insert abstract suspend fun insertWearEvent(event: WearEventEntity)

    @Query("UPDATE clothing_items SET lastWornAt = :wornAt, timesWorn = timesWorn + 1 WHERE id = :itemId")
    abstract suspend fun updateLastWorn(itemId: ClothingId, wornAt: Long)
}
```

If condition-change history ever becomes useful (beyond the current `condition` field on the item), add it the same way — an optional event table, not a replacement for the authoritative field:

```kotlin
@Entity(tableName = "condition_events")
data class ConditionEventEntity(
    @PrimaryKey val id: String,
    val itemId: ClothingId,
    val previousCondition: Condition,
    val newCondition: Condition,
    val changedAt: Long,
    val notes: String?
)
```
`ClothingItemEntity.condition` remains the directly-queryable, authoritative value; this table only exists to answer "when did this change and why," written in the same transaction as the condition update.

**Indexing:** add `@Index` on `type`, `brand`, `colorPrimary`, `status` — your filter screen will query these constantly and you want it snappy on a 500+ item closet, not just a demo dataset of 20.

**Search:** add an FTS4 virtual table (Room supports this natively) over `name`, `brand`, `type`, `colorPrimary` so "blue nike jacket" works as free-text search, not just structured filters. This is a small addition that makes a large closet feel instantly navigable.

**Migrations:** start Room with `exportSchema = true` from day one and commit schema JSON to git. You *will* add fields (seasonal tags, more purchase metadata) — plan the migration path before you need it, not after your test data is stuck on v1.

### 3.1a User Profile & Sizing

The original schema treated "size" as a loose string on the item and body shape as a coarse enum — not enough to drive a realistic avatar or any real fit logic. This needs its own entity, separate from `ClothingItemEntity`, since it's a single user profile (or possibly a few, if you ever support multiple household members) rather than a per-item concern.

```kotlin
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String = "default",
    // raw measurements, in metric internally (convert for display per locale/preference)
    val heightCm: Float?,
    val weightKg: Float?,
    val chestBustCm: Float?,
    val waistCm: Float?,
    val hipCm: Float?,
    val inseamCm: Float?,
    val shoulderWidthCm: Float?,
    val shoeSizeEu: Float?,
    val shoeSizeSystem: SizeSystem = SizeSystem.EU,   // stored as enum, converted for display — see §3.1a sizing note below
    val bodyShape: String?,              // optional coarse tag: pear/rectangle/athletic/etc, for avatar base mesh selection
    val updatedAt: Long
)
```

Measurements are **optional and incremental** — don't force a 7-field onboarding form before someone can add their first t-shirt. Prompt for height + one or two key measurements at onboarding, and let the rest be filled in opportunistically (e.g. "add your waist measurement to get fit predictions" as a dismissible nudge).

Clothing sizes need their own structured type instead of a free string, because "M" means different things per brand/region and you can't do meaningful fit comparison against a raw label:

```kotlin
enum class SizeSystem { US, UK, EU, ALPHA, UK_SHOE, US_SHOE, JP, UNKNOWN }

data class GarmentSize(
    val label: String,           // "M", "32", "9.5" — what's printed on the tag
    val system: SizeSystem,      // defaults to UNKNOWN if the tag's system can't be confidently identified
    val category: SizeCategory,  // TOPS, BOTTOMS, SHOES, OUTERWEAR — sizing scales differ by category
    val measuredChestCm: Float? = null,  // optional: garment's own measurements if known/scanned
    val measuredWaistCm: Float? = null,
    val measuredLengthCm: Float? = null
)
```

**Storage — flatten, don't embed as JSON:** the original draft said "embedded/JSON column," which papered over a real distinction. A JSON blob is opaque to SQL — you can't filter, index, or query into it without pulling every row and parsing in Kotlin. Use Room's `@Embedded` instead, which flattens `GarmentSize`'s fields into real columns on `ClothingItemEntity` (`sizeLabel`, `sizeSystem`, `sizeCategory`, `sizeMeasuredChestCm`, etc.) — same ergonomics in code (`item.size.label`), but genuinely queryable and indexable, e.g. "show all size M shirts" becomes a plain indexed `WHERE` clause instead of a full-table JSON scan.

```kotlin
data class ClothingItemEntity(
    // ...
    @Embedded(prefix = "size_") val size: GarmentSize?
)
```

**Sizing reality check (relevant given a Kampala market with US/UK/EU/Asian sizing all in circulation on different garments):** `SizeSystem.UNKNOWN` is a first-class, expected value, not an error state. `EstimateFitUseCase` must degrade gracefully when it can't confidently map a garment's system to the user's metric measurements — showing no fit signal rather than guessing, and never silently comparing across incompatible systems as if they were equivalent.

**Fit logic (`:core:domain`, new use-case `EstimateFitUseCase`):**
- v1: rule-based comparison — if garment has known measurements and user profile has corresponding measurements, compute a rough tight/true-to-size/loose signal per category. This is genuinely useful (flags "this'll be snug") without needing any ML or external size-chart data.
- v2 (optional, later): a bundled static size-chart table (brand → region → label → cm ranges) for common brands, used to *infer* garment measurements when the user only enters "Zara, M" rather than exact cm. This is real data-entry work (a lookup table you'd maintain), so treat it as a nice-to-have, not a Phase 1–2 dependency — the app is fully useful without it.

**Feeds the avatar too:** `AvatarConfig` should derive proportions from `UserProfileEntity` (height, waist/chest/hip ratios) rather than being a manually-picked enum disconnected from real measurements. Update the earlier `AvatarConfig`:

```kotlin
data class AvatarConfig(
    val heightCm: Float,
    val chestBustCm: Float?,
    val waistCm: Float?,
    val hipCm: Float?,
    val skinTone: SkinTone,
    val animationEnabled: Boolean
) {
    companion object {
        fun fromUserProfile(profile: UserProfileEntity, skinTone: SkinTone): AvatarConfig =
            AvatarConfig(
                heightCm = profile.heightCm ?: DEFAULT_HEIGHT_CM,
                chestBustCm = profile.chestBustCm,
                waistCm = profile.waistCm,
                hipCm = profile.hipCm,
                skinTone = skinTone,
                animationEnabled = true
            )
    }
}
```

The 2D layered-illustration renderer (Phase 3) can use height + waist/chest/hip ratios to pick from a small set of body-proportion variants (say, 5–7 silhouettes) rather than one fixed body — enough to feel personal without needing continuous 3D morphing.

### 3.2 DataStore (Proto)

Use **Proto DataStore**, not Preferences DataStore, for anything structured (theme config, feature toggles, avatar config). Preferences DataStore is fine for flat key-value flags but you'll regret it the moment `AvatarConfig` grows past 3 fields.

```protobuf
message UserPreferences {
  ThemeMode theme_mode = 1;
  bool animations_enabled = 2;
  bool avatar_module_enabled = 3;
  bool metadata_module_enabled = 4;
  AvatarConfig avatar_config = 5;
}
```

### 3.3 Repository pattern

```kotlin
interface ClothingRepository {
    fun observeItems(filter: ClosetFilter): Flow<List<ClothingItem>>
    suspend fun addItem(item: ClothingItem)
    suspend fun logWear(itemId: String, context: String?)
    suspend fun updateCondition(itemId: String, condition: Condition)
}
```

Repositories live in `:core:domain` as interfaces, implemented in `:core:database`. This is the seam that lets you unit test use-cases with fakes instead of an in-memory Room DB for every test.

---

## 4. Domain Layer (Use-Cases)

Keep these as plain classes with `operator fun invoke()`, one responsibility each:

- `AddClothingItemUseCase`
- `LogWearEventUseCase` — increments `timesWorn`, sets `lastWornAt`, optionally links to an `Outfit`
- `UpdateConditionUseCase`
- `CreateLaundryLoadUseCase` / `CompleteLaundryLoadUseCase`
- `GetClosetAnalyticsUseCase` — returns never-worn, over-rotated, color/type distributions
- `GenerateOutfitSuggestionUseCase` — starts rule-based (weather tag + underused items), can later become genuinely smarter, but resist ML here for v1; it's a distraction from the core value

**Analytics should read primarily from event history, not just the snapshot.** `timesWorn` on `ClothingItemEntity` stays as a cheap denormalized counter for list rendering (per §3's rationale), but that's a performance shortcut, not the analytics substrate. Anything richer queries `WearEventEntity`/`ConditionEventEntity` directly:

- `GetWearStreakUseCase` — consecutive-day or consecutive-week wear patterns per item or per tag.
- `GetTimeToRepairUseCase` — elapsed time between a `ConditionEvent` marking an item `NEEDS_REPAIR` and the next event moving it back to `GOOD`.
- `GetContextBreakdownUseCase` — wear distribution by `WearEvent.context` (work vs casual vs sport), which the denormalized counter can't answer at all since it has no context dimension.

This is the same current-state/event split from §3, just stated as a domain-layer rule: current-state fields answer "what is it right now," event tables answer "how did it get there and how often" — and the interesting analytics almost always live in the second category.

Use-cases depend only on repository interfaces — never on Room or DataStore directly. This is what makes `:core:domain` a pure-Kotlin module with fast unit tests.

---

## 5. Presentation Layer

Standard MVI-ish unidirectional flow, since you already know Compose:

```kotlin
class ClosetViewModel(
    private val observeItems: ObserveClosetItemsUseCase,
    private val logWear: LogWearEventUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(ClosetUiState())
    val uiState: StateFlow<ClosetUiState> = _uiState.asStateFlow()

    fun onFilterChanged(filter: ClosetFilter) { /* update state, re-collect */ }
    fun onWearNow(itemId: String) { viewModelScope.launch { logWear(itemId) } }
}
```

One `UiState` data class per screen, one sealed `UiEvent`/`UiAction` for user intents. Nothing novel here — the point is to keep it boring and consistent across all five feature modules so any of them is instantly familiar.

### Navigation

Use a single `:app`-level `NavHost` with each feature module exposing its own `NavGraphBuilder` extension (`closetGraph()`, `outfitGraph()`, etc.) — this keeps features decoupled from each other and from the top-level nav structure, and is a well-trodden pattern for multi-module Compose apps.

---

## 6. Design System (`:core:designsystem`)

Since Material 3 Expressive is central to your pitch, treat this as a first-class module, not a `Theme.kt` file:

- **Tokens**: color roles, shape scale, typography scale, motion scheme — defined once, referenced everywhere.
- **Motion**: Expressive's shared-element and spring-based transitions are a real differentiator — use `AnimatedContent`/`SharedTransitionLayout` for item-detail transitions, outfit-builder drag interactions, and closet filter changes, and especially the closet → outfit → avatar hero transition, since that chain is the app's signature moment. This is where "premium" actually shows up to a user, more than any single screen's layout. Define an explicit motion scheme (spring stiffness/damping presets — a calmer spring for list/filter changes, a bouncier one for the avatar reveal) as tokens in `:core:designsystem` rather than tuning each transition ad hoc per screen.
- **Shape**: a defined shape scale, not just M3 defaults — e.g. slightly rounded corners for `WardrobeCard`, a more expressive/asymmetric shape reserved for primary CTAs (log wear, save outfit) so they read as the app's signature action rather than blending into the rest of the UI.
- **Components**: wrap M3 components once (`WardrobeCard`, `WardrobeFilterChip`, `WardrobeStatusBadge`) even when they're thin wrappers — it gives you one place to adjust styling later instead of hunting through five feature modules.
- **Dynamic color**: support Material You dynamic color as default, with your own expressive palettes as an explicit alternate theme choice in onboarding, not the only option.
- **Designed NoOp/empty states**: "wired in but invisible" (Phase 1's `NoOpAvatarRenderer`/`impl-noop` metadata) needs an actual design pass, not just an absence. A `WardrobeCard` or item-detail screen with no avatar slot filled and no metadata shouldn't read as broken or half-finished — design a deliberate empty/placeholder layout (e.g. a clean silhouette outline instead of a blank space where the avatar would go, a "add measurements to preview fit" prompt instead of a missing field) so disabled modules feel like a calm default state, not a bug.

---

## 7. Avatar Module — Revised

Given the earlier caution about scope: this is now explicitly two separate implementations behind one interface, and only one ships in v1.

### 7.1 `:avatar:api`

```kotlin
enum class AvatarAnimationState { IDLE, WALK, TURN, SHOWCASE }

interface AvatarRenderer {
    @Composable
    fun Render(
        config: AvatarConfig,
        outfit: List<ClothingItem>,       // resolved internally to slots via ClothingItem.avatarSlot
        animationState: AvatarAnimationState,
        modifier: Modifier
    )
}
```

**Why animation state is part of the interface from day one, not added later:** if `impl-2d`'s `Render` signature only accepts `(config, outfit, modifier)` and `impl-3d` later needs an animation-state parameter to drive its skeletal animation, that's a breaking interface change touching every call site. Defining `AvatarAnimationState` now — even though `impl-2d` will only meaningfully support `IDLE`/`WALK` via its sprite-sheet approach — means `impl-3d` can add richer states later (or a camera-control parameter, if that becomes relevant) without an interface break, since it's additive to an enum rather than a new required parameter. `impl-2d` simply ignores states it can't express.

`AvatarConfig` itself is defined once, in §3.1a, derived from `UserProfileEntity` — there's no separate `BodyType` enum. The renderer infers silhouette directly from height/chest/waist/hip ratios rather than a hand-picked category; this removes a redundant, easily-inconsistent field (the enum could disagree with the actual measurements) in favor of one source of truth.

### 7.2 `:avatar:impl-2d` (v1 — build this)

- Layered flat/vector illustrations (base body → base layer → mid layer → outer layer → shoes), composited with Compose `Canvas`/`Image` layering by z-order per `avatarSlot`.
- "Movement" achieved via Compose animation: subtle idle sway, a walk-cycle using a small sprite sheet (8–12 frames) rather than true 3D skeletal animation. This gets visual life without a rendering engine.
- Garment fit approximated by slot + simple width/length scaling per body type — not true cloth simulation.
- Fully achievable solo in weeks, not months, and ships with the rest of the app.

### 7.3 `:avatar:impl-3d` (v2+, only after 2D ships and you still want it)

- Godot embedded via `AndroidView` + Godot's Android export, or a lightweight custom Vulkan/OpenGL renderer if you want to avoid a second engine's build toolchain inside a Kotlin project.
- Treat this as its own spike/prototype project *outside* the main app first. Don't let it block or share a timeline with Phase 1–2. If it works, wire it in behind `:avatar:api` with zero changes to feature code.

---

## 8. Metadata Module (Barcode/SKU + Care-Tag OCR)

Two implementations behind the same module, but they don't share an opt-in reason — worth keeping distinct:

- `:metadata:api` defines `MetadataProvider.lookup(barcode: String): ClothingMetadata?`
- Default bound implementation is `:metadata:impl-noop`.
- **`:metadata:impl-barcode`** — CameraX + ML Kit barcode scanning, hits a pluggable network provider. Opt-in specifically because it's *network-dependent*: it's gated behind the DataStore flag for privacy/offline-first reasons, exactly as originally scoped.
- **`:metadata:impl-caretag-ocr`** (new, distinct implementation) — CameraX + ML Kit Text Recognition reading garment care-tag text and laundry symbols, entirely on-device, zero network calls. This is a genuinely different case from barcode lookup: there's no privacy/offline tradeoff to gate behind a toggle, only a camera permission prompt like any photo-capture feature already in the app. Feeds directly into `CreateLaundryLoadUseCase` (auto-suggesting wash temperature/care requirements) without touching the network-gated flag at all.
- Every scanned result — from either implementation — still requires manual user confirmation before writing to Room; never auto-commit third-party or OCR'd data.

Practically: ship `:metadata:impl-caretag-ocr` whenever it's convenient (it doesn't compromise the offline-first pitch, so it doesn't need to wait behind the same "optional, opt-in" framing as barcode scanning) — the barcode module remains the one held back in the roadmap specifically because it's the one with a real network/privacy tradeoff.

Give barcode scanning its own settings page — not just a toggle buried in general settings — that plainly states what happens once enabled: what data leaves the device, to what kind of endpoint, and that it's entirely optional. That level of transparency matches what the offline-first pitch already implies; an unlabeled switch undersells how deliberate that boundary actually is.

---

## 8a. Suggestion & Weather — Interface-First

`GenerateOutfitSuggestionUseCase` becomes a proper interface so the rule-based version isn't a dead end:

```kotlin
interface SuggestionEngine {
    suspend fun suggest(closet: List<ClothingItem>, context: SuggestionContext): List<OutfitSuggestion>
}
```

- `RuleEngine` (v1) — underused-item + basic tag matching. Ships in Phase 2.
- `WeatherEngine` (optional) — wraps a `WeatherProvider` interface (`NoOpWeatherProvider` by default; a real implementation would need network, so it's opt-in exactly like metadata). "Rain forecast → recommend waterproof shoes" becomes a straightforward addition later with zero changes to `:feature:analytics`.
- `AIEngine` (future, not scoped here) — same interface, different implementation, if you ever want it. If you get here, it doesn't have to break the offline-first promise: a quantized model run on-device via MediaPipe LLM Inference or ExecuTorch keeps everything local, no cloud call required. Worth naming now so it's clear the roadmap's "AI" placeholder was never assumed to mean a server — but treat the storage footprint (quantized models are still commonly hundreds of MB to a few GB) and battery/thermal cost of on-device inference as real constraints to evaluate before committing, not a given. This stays a genuine future phase, well past everything else in this document.

## 8b. Domain Model Layer

Room entities should never leak into the UI directly — keep a clear mapping boundary:

```
Room Entity  →  Mapper  →  Domain Model  →  UI Model (if it diverges)
```

`ClothingItemEntity` (Room-specific, has converters/annotations) maps to a plain `ClothingItem` domain data class in `:core:model` that use-cases and ViewModels operate on. This is what lets `:core:domain` stay a pure-Kotlin module with no Room dependency, and it's the seam that makes swapping storage engines (unlikely, but also unit-testing use-cases) trivial. Keep mappers as simple extension functions (`ClothingItemEntity.toDomain()`) — no need for a heavyweight mapping library at this scale.

## 8c. Backup Module

Prioritized **ahead of** barcode scanning — an offline-first app's biggest real risk is data loss on a lost/broken phone, and this benefits every user, not just convenience for data entry. This is backup/export (preserve-and-restore a local dataset), deliberately not sync (continuously reconciling multiple mutable copies) — the latter is out of scope entirely unless multi-device sync is someday approved as an actual product feature, since a placeholder interface today wouldn't resolve any of the real decisions that feature would require (identity/auth, conflict resolution, tombstones, versioning, retry semantics).

Even the interface doesn't need to exist until Phase 5 work actually begins — no value in carrying it as scaffolding through Phases 0–4. When it's time:

```kotlin
interface BackupManager {
    suspend fun export(destination: BackupDestination): BackupResult
    suspend fun restore(source: BackupSource): RestoreResult
}
```

```
:backup:api         → BackupManager interface (introduced at Phase 5, not before)
:backup:impl-local  → JSON/ZIP export to device storage (v1)
:backup:impl-drive  → Google Drive backup (v2, opt-in, same pattern as metadata)
```

v1 is a full local export (JSON + copied images into a zip) triggerable manually and optionally on a schedule via WorkManager — no cloud dependency required to get real safety here. Encrypt the archive if it contains photos (see §8f).

## 8d. Continuous Integration

Small but worth specifying up front so it's habitual, not retrofitted:

- `ktlint` + `detekt` on every PR
- Room schema export verification (fails the build if a schema changed without a migration)
- Unit tests (`:core:domain`, `:core:database`)
- Compose screenshot tests for `:core:designsystem` components
- Release build sanity check

## 8e. Accessibility

Worth stating explicitly since Material 3 Expressive leans heavily on motion:

- Respect system "reduce motion" — gate avatar walk-cycles and shared-element transitions behind `LocalAccessibilityManager`/animation-scale checks, not just a personal preference toggle.
- Large font / dynamic type support throughout `:core:designsystem` typography tokens.
- TalkBack labels on all interactive closet/filter/avatar controls.
- Concrete contrast targets, not just "check by eye": 4.5:1 for body text, 3:1 for larger text and graphical elements (icons, status badges, filter chips). The clustered small elements — status badges, filter chips sitting against card backgrounds — are the ones most likely to quietly fail this in both the dynamic-color and custom expressive palettes, so check those specifically rather than assuming the theme as a whole passes.
- Destructive actions (retire an item, delete an outfit, discard a laundry load) get a real confirmation dialog with proper focus behavior and clear labeling — kept sparse and reserved for genuinely irreversible actions, since over-using confirmation dialogs elsewhere is exactly what the undo-via-snackbar pattern (§Phase 4) is meant to replace.

## 8f. Security & Privacy

Not because this is a cloud app — because wardrobe photos and body measurements are personal data, and the offline-first pitch is partly a privacy pitch:

- No telemetry/analytics SDKs by default.
- No network access unless the user explicitly opts into metadata lookup or cloud backup — both already gated behind DataStore flags per their respective sections.
- Encrypt local backup archives (§8c) if they contain photos or measurements — SQLCipher for the DB or simple archive-level encryption for the export file. If the on-device Room DB itself is encrypted (SQLCipher-backed Room, worth considering given item photos may include people), make sure `backup:impl-local` still produces a restorable archive from that encrypted source, not a plaintext export that undermines the point.
- Consider an optional app-level PIN/biometric lock (`BiometricPrompt`) as a settings toggle — off by default so it doesn't add friction for someone who doesn't need it, but a real option given the photos involved.
- `UserProfileEntity` measurements never leave the device unless the user explicitly exports/backs up.
- **Trust UX / copy tone:** the privacy story is only as good as how clearly it's communicated. Settle on consistent, plain-language copy early — "All wardrobe data stays on this device; nothing is uploaded unless you turn on [metadata lookup / cloud backup]" — and reuse it verbatim wherever the topic comes up (onboarding, settings, the barcode settings page from §8), rather than re-explaining it differently in five places.

---

## 8g. Resilience

A few offline-first-specific failure modes worth planning for explicitly, since "it's all local" doesn't mean "it can't fail":

- **DB corruption / recovery:** decide the actual fallback now rather than discovering it via a support request to yourself later. A reasonable default: detect Room open failures, offer to restore from the most recent local backup archive (§8c) if one exists, and otherwise present a clear "your data may be corrupted, here's what you can try" screen rather than a silent crash loop.
- **Low-storage behavior:** define what happens when an image save or backup export fails due to disk space — surface a clear warning and let the item save without the photo (text-only entry) rather than losing the whole operation. Graceful degradation beats a blocked flow.
- **Schema evolution:** already covered in §3 (exportSchema + committed schema JSON), but worth restating here as a resilience concern, not just a dev-workflow one — untested migrations are a real path to data loss on an update.

## 8h. Observability & Dev Tooling

Genuinely useful for a solo project where you're both the developer and the only one who'll notice something's wrong:

- A debug screen gated behind a build flag or hidden gesture — Room table row counts, recent wear/condition events, last backup timestamp. Cheap to build, disproportionately useful when something looks off in your own dogfooding.
- Standardized log tags per domain area (wear, laundry, condition, avatar) so `adb logcat` filtering is usable instead of noise.
- Feature flags via the existing DataStore preferences (already used for avatar/metadata module toggles) double as a dev convenience — flip `impl-2d` vs a future `impl-3d`, or force `NoOp` providers, without a rebuild.

## 8i. Multi-Profile and Future Sync — Explicitly Deferred

Two different kinds of "not now":

- **Multi-profile** (supporting "Me"/"Partner"/"Kids" as separate wardrobes) is a genuine product-scope question, not an architecture one — it's not clear this app needs it. If it's ever wanted, it's an ordinary migration (add a nullable `profileId` foreign key to `ClothingItemEntity`, `OutfitEntity`, etc.) rather than a re-architecture, so there's no cost to deciding this later rather than designing for it speculatively now.
- **A generic `SyncEvent`/`ChangeLog` concept "just in case"** is declined for the same reason the `SyncEngine` interface was declined earlier in this document: having `WearEventEntity`/`ConditionEventEntity` already doesn't meaningfully prepare the app for real sync, because sync's actual hard problems — conflict resolution, identity/auth, tombstones, retry semantics — aren't touched by adding a changelog table today. If multi-device sync is ever approved as an actual feature, it deserves its own design pass at that time, informed by requirements that don't exist yet — not a placeholder carried through every phase between now and then.

## 8j. Project Meta — i18n, Licensing, Distribution

Lighter-weight than the rest of this document, but worth deciding early since they're cheap now and annoying to retrofit:

- **Internationalization:** decide up front whether v1 is English-only with freeform sizing (the pragmatic default, given `SizeSystem` already handles the regional-sizing dimension separately from language) or whether multi-locale is a real near-term goal. Either way, keep UI strings centralized (string resources, not hardcoded in feature modules) so the decision is cheap to revisit later — this costs nothing to do from the start and is genuinely painful to retrofit across five feature modules.
- **Licensing & contribution model:** if this is FOSS-headed, pick a license (MIT/Apache-2/GPL) and decide on a contribution model (CLA/DCO, code-style expectations) before the first outside contributor shows up, not after. A lightweight `/docs/adr` folder with short Architecture Decision Records is worth it mainly for *this document's* declined-on-purpose choices (no sync scaffold, no event sourcing, current-state persistence) — so a future contributor sees those as deliberate rather than reopening settled debates.
- **Release & distribution:** decide the canonical release channel (F-Droid + GitHub APK vs. Play Store, or both) early, since it affects the privacy policy and any telemetry/permission decisions upstream of it. A simple semver cadence tied to the phase milestones already in this roadmap (e.g. "avatar v1" = Phase 3 complete) keeps releases meaningfully scoped rather than arbitrary.

---

## 9. Dependency Injection

Hilt is the pragmatic choice given Google's continued investment and your existing Kotlin fluency — Koin is fine too if you prefer runtime DI, but Hilt's compile-time graph will catch module-wiring mistakes (like a feature accidentally depending on `:avatar:impl-3d` directly) at build time rather than runtime.

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AvatarModule {
    @Provides
    fun provideAvatarRenderer(prefs: UserPreferences): AvatarRenderer =
        if (prefs.avatarModuleEnabled) Avatar2DRenderer() else NoOpAvatarRenderer()
}
```

---

## 10. Testing Strategy

- `:core:domain` — pure unit tests, fake repositories, no Android runtime needed (fast, run on every commit).
- `:core:database` — Room in-memory DB tests for DAOs and migrations, including explicit tests around `@Transaction` methods like `logWear` to verify the counter and event insert never drift apart.
- `:feature:*` ViewModels — unit tests with fake use-cases, Turbine for Flow assertions.
- Compose UI — a handful of screenshot tests (Paparazzi or Compose Preview screenshot testing) for the design system components, not exhaustive UI tests for every screen — diminishing returns for a solo project.
- **Offline-first scenario tests** (a real gap in the original plan): process death/restore, device reboot mid-write, low-storage conditions during image save, and schema migration correctness across a couple of version jumps. These are exactly the class of bug that "works on my dev device" testing misses, and they're specific to the offline-first promise this app is making.
- **Performance**: Macrobenchmark for cold-start, closet scrolling with a large (500+ item) dataset, and the avatar screen transition; Microbenchmark for hot DAO queries and analytics computation. Not needed from Phase 0, but worth introducing once Phase 1–2 are stable enough that regressions would actually be visible — a dedicated `:test:benchmark` module keeps this from staying purely aspirational. Give these actual numeric budgets rather than vague "fast" targets — e.g. cold start under ~1.5s on midrange hardware, closet scroll holding 60fps with 500+ items, avatar transition frame time under 16ms — so a regression has a concrete threshold to trip, not just a feeling that something got slower.

---

## 11. Revised Phased Roadmap

**Phase 0 — Scaffolding (few days)**
Module graph above, empty Hilt graph, M3 Expressive theme skeleton, Room DB with schema export wired to CI/git.

**Phase 1 — Core Closet MVP**
`:feature:closet`, `:feature:history`, manual item CRUD (with structured `GarmentSize`, not a free string), filters, wear logging, and a lightweight optional profile/measurements screen (`UserProfileEntity`). `NoOpAvatarRenderer` and `impl-noop` metadata wired in but invisible. Ship this and dogfood it on your own wardrobe before building anything else.

**Phase 2 — Laundry, Condition, Analytics**
`:feature:laundry`, `:feature:analytics`, suggestion rules, and `:metadata:impl-caretag-ocr` (care-tag scanning feeding `CreateLaundryLoadUseCase`) — this can ship here rather than waiting with barcode scanning, since it's fully on-device and doesn't carry the same network/privacy tradeoff.

**Phase 3 — 2D Avatar**
`:avatar:impl-2d` — layered illustration renderer, slot-based outfit compositing, idle/walk animation.

**Phase 4 — Outfit Builder polish + Expressive motion pass**
Shared-element transitions, drag-to-build outfits on the avatar, refined onboarding, undo-via-snackbar for wear/delete/laundry/retire actions instead of confirmation dialogs.

**Phase 5 — Backup module**
`:backup:impl-local` — local JSON/ZIP export, optionally scheduled via WorkManager. Ships before barcode scanning: this protects every user's data, not just a data-entry convenience.

**Phase 6 — Optional: Barcode/SKU module**
`:metadata:impl-barcode` only — the network-dependent, privacy-gated piece. Explicitly opt-in, isolated network path.

**Phase 7 — Optional/exploratory: 3D avatar spike**
Separate prototype repo/project. Only merges into `:avatar:impl-3d` if it proves viable without derailing everything else.

---

## 12. What Changed From the First Draft

- Avatar is now interface-first (`:avatar:api`) so the risky 3D work can be deferred or dropped without touching the rest of the app.
- 2D avatar promoted to a real, shippable Phase 3 — no longer skipped in favor of jumping straight to 3D.
- Proto DataStore replaces Preferences DataStore for structured config.
- Explicit indexing and migration strategy for Room from day one.
- Metadata module confirmed as strategy-pattern plug-in, network gated behind an explicit preference flag, not just a settings toggle in spirit.
- Testing strategy scoped to what's sustainable solo, not exhaustive.
- Added `UserProfileEntity` (height, weight, chest/waist/hip, shoe size) and a structured `GarmentSize` type replacing the old free-string `size` field — enables a real (optional) fit-estimation use-case and lets the avatar's proportions derive from actual measurements instead of a disconnected enum.

**Round 3 (post-review):**
- Junction tables (`OutfitItemCrossRef`, `LaundryLoadItemCrossRef`) replace JSON `List<String>` columns for real foreign keys, cascade deletes, and joinable queries.
- Enums (`ClothingType`, `Condition`, `Status`, `SizeSystem`, `SizeCategory`) replace raw strings for categorical fields; value-class IDs (`ClothingId`, `OutfitId`, etc.) prevent mixing up identifiers at compile time.
- `ImageEntity` split out from `ClothingItemEntity` for multi-photo/thumbnail support.
- Removed `BodyType` entirely — the avatar renderer infers silhouette from `UserProfileEntity` measurements directly, one source of truth instead of two that could disagree.
- Added FTS4 search, undo-via-snackbar, cost tracking fields (`purchasePrice`/`purchaseDate`/`purchaseLocation`), `SuggestionEngine`/`WeatherProvider` interfaces, an explicit domain-model mapping layer, a CI checklist, and accessibility/security sections.
- **Backup module (`:backup:api`) added and moved ahead of barcode scanning in the roadmap** — it protects every user's data, which matters more than a data-entry convenience.
- **Declined:** a `SyncEngine` abstraction and full event-sourcing. Both are legitimate patterns in general, but a placeholder interface solves none of the real problems either would eventually require (identity/auth, conflict resolution, tombstones, versioning, retry semantics for sync; projections, replay correctness, schema evolution for event sourcing) — it would only add a dependency boundary and a NoOp implementation to carry indefinitely. **Settled position:** the app uses current-state persistence, not event sourcing — Room entities are authoritative, event tables exist selectively for history/auditability, and any dual write happens in one `@Transaction` (see §3, `logWear` example). Backup/export is scoped as preserve-and-restore of a local dataset; sync (reconciling multiple mutable copies) is explicitly out of scope unless multi-device sync is someday approved as its own product feature — at which point it deserves its own design pass, not a seam bolted on today. Even the `BackupManager` interface itself is deferred until Phase 5 rather than scaffolded early, since it has no use until that work begins.

If you want, I can generate actual Kotlin skeletons next — Gradle module setup, the Room entities/DAOs (including the junction tables and converters), and a barebones `:avatar:api` + `NoOpAvatarRenderer` so Phase 0 is a paste-and-run starting point.

**Round 4 (post-review):**
- Fixed a real inconsistency: `GarmentSize` now stored via Room `@Embedded` (flattened, indexable columns) rather than the previously ambiguous "embedded/JSON column" — JSON would have made "filter by size" impossible without a full-table scan.
- `SizeSystem` gains an explicit `UNKNOWN` fallback; `EstimateFitUseCase` must degrade to "no fit signal" rather than guess when a garment's sizing system can't be confidently mapped — relevant given how mixed US/UK/EU/Asian sizing is in practice.
- Split care-tag OCR (`:metadata:impl-caretag-ocr`) out from barcode lookup as its own implementation: it's fully on-device via ML Kit Text Recognition with no network call, so it doesn't belong behind the same privacy/network opt-in gate as barcode scanning — moved earlier in the roadmap (Phase 2, feeding `CreateLaundryLoadUseCase`) since it doesn't carry barcode's tradeoff.
- Named a concrete on-device path for the future `AIEngine` (MediaPipe/ExecuTorch quantized models) so the offline-first promise holds even if the suggestion engine eventually goes AI-assisted — flagged with honest storage/battery caveats, still a genuine future phase.
- Design system now explicitly calls for designed empty/placeholder states for disabled modules (no avatar, no metadata) rather than leaving "wired in but invisible" underspecified.

**Round 5 (post-review):**
- ID generation specified as ULID rather than plain UUID — same collision safety, but lexically sortable by creation time, so chronological queries on `WearEventEntity`/`ImageEntity` don't need a separate sort/index.
- Added an explicit caution on enum `TypeConverter` migration risk: renaming a stored enum constant silently orphans existing rows; treat renames as a real migration event, not a free refactor.
- Formalized event-first analytics as a domain-layer rule: `timesWorn` stays a denormalized performance shortcut, but real analytics (`GetWearStreakUseCase`, `GetTimeToRepairUseCase`, `GetContextBreakdownUseCase`) read from `WearEventEntity`/`ConditionEventEntity` directly, since those are the only place per-context and time-based patterns actually live.
- `AvatarRenderer.Render` now takes an explicit `AvatarAnimationState` parameter from day one, so `impl-2d` and a future `impl-3d` share one interface without a breaking change when richer animation/camera control is eventually needed.
- Barcode module gets its own settings page (not a buried toggle) explaining exactly what data leaves the device and when.
- Design tokens specified more concretely: an explicit motion scheme (spring presets per transition type) and shape scale (rounded cards vs. expressive CTA shapes), rather than leaving "Expressive" as a general aspiration.

**Round 6 (post-review):**
- Testing strategy expanded with offline-first scenario tests (process death, low-storage mid-write, migration correctness) and a `:test:benchmark` module for Macrobenchmark/Microbenchmark, introduced once Phase 1–2 are stable rather than from day one.
- Accessibility section gets concrete contrast targets (4.5:1 body / 3:1 large-and-graphical) and explicit guidance on when destructive-action dialogs are warranted vs. when undo-via-snackbar should be used instead.
- Added a Resilience section (§8g): DB corruption recovery path via the existing local backup, graceful low-storage degradation (save item without photo rather than blocking the whole operation).
- Added an Observability & Dev Tooling section (§8h): gated debug screen, standardized log tags, DataStore-based feature flags doubling as a dev convenience alongside their existing module-toggle purpose.
- **Declined again, consistently:** a generic `SyncEvent`/`ChangeLog` scaffold "for future sync" — same reasoning as the earlier `SyncEngine` decline; existing event tables don't meaningfully de-risk a future sync feature, since sync's real hard problems (conflict resolution, identity, tombstones) aren't touched by adding a changelog now. Multi-profile support is noted as a deferred product-scope decision instead, since — unlike sync — it's a cheap ordinary migration to add later if ever wanted, so there's no cost to not designing for it now.

**Round 7 (post-review, likely final architecture pass):**
- Security section extended: optional encrypted Room DB (SQLCipher) alongside the already-planned encrypted backup archive, and an opt-in PIN/biometric app lock — off by default, given the photos involved but not everyone needing it.
- Added a consistent trust-UX copy note — one plain-language privacy statement reused verbatim across onboarding/settings/barcode page, rather than re-explained differently in five places.
- Performance budgets made concrete (cold start ~1.5s, 60fps closet scroll at 500+ items, <16ms avatar transition frame time) rather than left as vague "should feel fast."
- Added a compact Project Meta section (§8j): i18n strategy (English-only v1 with freeform sizing as the pragmatic default, centralized strings regardless), licensing/contribution model, and release/distribution channel (F-Droid/GitHub vs. Play Store) — lighter-weight than the rest of the document but cheap to decide now and painful to retrofit.
- This is a reasonable point to treat the architecture as settled. Seven rounds have covered product shape, module boundaries, data modeling, sizing/i18n, avatar evolution, metadata, backup, design tokens, testing, accessibility, resilience, observability, security, and project meta — with declined items (sync scaffolding, event sourcing) explicitly reasoned through rather than left ambiguous. Further changes from here should be driven by actual product decisions (e.g. "I want multi-profile now") or by things learned while writing code, not by further abstract review.
