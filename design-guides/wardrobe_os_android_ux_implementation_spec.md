# Wardrobe OS — Android UI/UX Implementation Spec

## 1. Purpose

This document translates the Wardrobe OS design vision into an Android-ready UI/UX specification.

It defines:

- Navigation structure
- Screen hierarchy
- Core user flows
- State models
- Component behavior
- Accessibility requirements
- Empty, error, and edge states
- Jetpack Compose implementation notes

---

## 2. Recommended App Architecture for UX

## 2.1 Main Navigation

Use bottom navigation on phones.

```text
Closet
Outfits
Laundry
History
More
```

### Destination Roles

| Destination | Purpose | Primary Mode |
|---|---|---|
| Closet | Browse and manage wardrobe items | Calm / Focused |
| Outfits | Build, save, and explore outfits | Calm / Playful |
| Laundry | Manage dirty items and laundry loads | Practical / Maintenance |
| History | View wear history and calendar | Calm / Practical |
| More | Settings, backup, repair queue, analytics, privacy | Practical / Maintenance |

## 2.2 Adaptive Navigation

On larger screens, tablets, and foldables:

- Use Navigation Rail.
- Support list-detail layouts.
- Keep the selected item visible while detail content opens beside it.

### Phone Layout

```text
BottomNavigation + single-pane screens
```

### Tablet/Foldable Layout

```text
NavigationRail + list/detail content
```

---

## 3. Information Architecture

```text
App
├── Closet
│   ├── Closet Grid
│   ├── Item Detail
│   ├── Add/Edit Item
│   └── Filter/Search
│
├── Outfits
│   ├── Saved Outfits
│   ├── Outfit Detail
│   ├── Outfit Builder
│   └── Avatar Preview
│
├── Laundry
│   ├── Laundry Dashboard
│   ├── Load Detail
│   └── Create/Edit Load
│
├── History
│   ├── Calendar View
│   ├── Day Detail
│   └── Wear Timeline
│
└── More
    ├── Repair Queue
    ├── Analytics
    ├── Backup & Restore
    ├── Privacy
    ├── Appearance
    └── Settings
```

---

## 4. Core Domain State Models

## 4.1 Clothing Item

A clothing item should separate visual, status, condition, and ownership concepts.

```kotlin
data class ClothingItemUiModel(
    val id: String,
    val name: String,
    val category: ClothingCategory,
    val imageUri: String?,
    val brand: String?,
    val primaryColorName: String?,
    val size: String?,
    val cleanliness: CleanlinessStatus,
    val condition: ConditionStatus,
    val ownership: OwnershipStatus,
    val lastWornLabel: String?,
    val wearCountLabel: String
)
```

## 4.2 Cleanliness Status

```kotlin
enum class CleanlinessStatus {
    Clean,
    Worn,
    Dirty,
    InLaundry
}
```

### UX Rules

| Status | Meaning | UI Treatment |
|---|---|---|
| Clean | Ready to wear | Positive or neutral chip |
| Worn | Used recently but not necessarily dirty | Neutral chip |
| Dirty | Should be washed | Muted warning chip |
| InLaundry | Currently assigned to a laundry load | Secondary chip |

## 4.3 Condition Status

```kotlin
enum class ConditionStatus {
    Good,
    NeedsRepair,
    InRepair,
    Repaired,
    WornOut
}
```

### UX Rules

| Status | Meaning | UI Treatment |
|---|---|---|
| Good | No action needed | No badge or subtle badge |
| NeedsRepair | User should repair item | Amber badge |
| InRepair | Currently being repaired | Tertiary badge |
| Repaired | Recently repaired | Positive confirmation |
| WornOut | Candidate for retirement | Muted warning badge |

## 4.4 Ownership Status

```kotlin
enum class OwnershipStatus {
    Active,
    Archived,
    Retired,
    Deleted
}
```

### UX Rules

- Active items appear in the main closet.
- Archived items are hidden but recoverable.
- Retired items remain in history but do not appear in active outfit suggestions.
- Deleted items should require confirmation.

Prefer **Retire item** over hard delete for clothes that have wear history.

---

## 5. Screen Specifications

# 5.1 Closet Screen

## Purpose

The main wardrobe browsing and management screen.

## Mode

Calm / Focused

## Primary Content

- Search bar
- Horizontal filter chips
- Wardrobe grid/list
- Floating action button or prominent add button

## Clothing Card Content

Each card should show:

- Image
- Name
- Category
- Cleanliness chip
- Condition badge if not Good
- Last worn label where useful

## Primary Actions

- Tap card: open item detail
- Long press card: contextual actions
- Add item
- Filter by category/status/tag
- Search

## Empty State

Title:

> Your closet is empty.

Body:

> Add your first item to start building your wardrobe.

Actions:

- Add item
- Try sample closet
- Restore backup

## Error State

If images fail to load:

- Show placeholder image.
- Keep item name and status visible.
- Offer retry only if the image source is recoverable.

---

# 5.2 Item Detail Screen

## Purpose

View and manage a single clothing item.

## Mode

Calm / Focused with light playful accents

## Sections

1. Hero image
2. Name and category
3. Cleanliness chip
4. Condition badge
5. Primary actions
6. Attributes
7. Wear history
8. Notes
9. Advanced actions

## Primary Action

- Wear now

## Secondary Actions

- Add to outfit
- Add to laundry
- Mark dirty
- Mark needs repair
- Edit

## Destructive / Sensitive Actions

- Retire item
- Archive item
- Delete item

## Wear Now Behavior

On tap:

1. Record wear event immediately.
2. Update last worn date.
3. Show snackbar.

Snackbar:

> Added to today’s wear history.

Actions:

- Undo
- Add context

---

# 5.3 Add/Edit Item Screen

## Purpose

Capture wardrobe items quickly.

## Recommended Structure

Use a full-screen form for complex edits. A modal bottom sheet may be used for quick capture, but full-screen is safer for photo and metadata entry.

## Form Sections

### Photo

Options:

- Take photo
- Choose from gallery
- Remove photo
- Use placeholder

### Required Details

- Name
- Category/type

### Optional Details

- Brand
- Color
- Size
- Season
- Tags
- Notes

### Status

Defaults:

- Cleanliness: Clean
- Condition: Good
- Ownership: Active

## Save Behavior

On successful save:

- Navigate to item detail, or
- Return to closet and highlight new/updated card.

## Draft Behavior

If the user exits with unsaved changes:

Dialog:

> Discard changes?

Actions:

- Keep editing
- Discard

Where possible, keep unsaved form state in memory.

---

# 5.4 Outfits Screen

## Purpose

Let users create, save, and reuse outfit combinations.

## Mode

Calm / Playful

## Content

- Saved outfit cards
- Suggested combinations
- Recently worn outfits
- Create outfit action

## Outfit Card Content

- Outfit name
- Thumbnail collage or hero image
- Item count
- Last worn label
- Optional context tags

## Empty State

Title:

> No outfits yet.

Body:

> Combine items from your closet to create your first outfit.

Actions:

- Create outfit
- View closet

---

# 5.5 Outfit Builder

## Purpose

Create an outfit from multiple clothing items.

## Layout

- Top: selected outfit preview
- Middle: selected item chips or rows
- Bottom: closet picker/filter

## Key Actions

- Add item
- Remove item
- Save outfit
- Preview on avatar if avatar is enabled

## Validation

Do not require a strict clothing formula. Users may save any combination.

Warning only when outfit is empty:

> Add at least one item to save this outfit.

---

# 5.6 Avatar Preview

## Purpose

Provide playful outfit exploration.

## Priority

P2 / Optional

## Mode

Playful / Exploratory

## UI Structure

- Avatar viewport
- Outfit carousel
- Pose controls
- Environment controls
- Motion intensity control if needed

## Reduced Motion Behavior

If reduced motion is enabled:

- Disable showcase flourish.
- Use static pose changes.
- Replace clothing transitions with crossfade.

---

# 5.7 Laundry Screen

## Purpose

Manage dirty clothes and laundry loads.

## Mode

Practical / Maintenance

## Content

- Dirty items summary
- Active laundry loads
- Recently completed loads
- Create load action

## Empty State

Title:

> Nothing in laundry.

Body:

> Items you mark as dirty will appear here.

Action:

- View closet

## Load Completion

When user taps **Mark washed**:

1. Items return to Clean.
2. Load moves to completed.
3. Snackbar appears.

Snackbar:

> Laundry marked clean.

Action:

- Undo

---

# 5.8 Repair Queue

## Purpose

Track clothes that need repair.

## Location

Under More by default.

If active repairs exist, surface a card on Closet or Laundry.

## Content

- Items needing repair
- Time since marked
- Notes
- Mark repaired action
- Retire item action

## Empty State

Title:

> No repairs needed.

Body:

> Items marked as needing repair will appear here.

---

# 5.9 History Screen

## Purpose

Let users review what they wore over time.

## Mode

Calm / Practical

## Views

- Calendar view
- Timeline view

## Calendar Cell

Should indicate:

- Wear event exists
- Outfit thumbnail or item count
- Today
- Selected date

## Day Detail

Show:

- Items worn
- Outfit if applicable
- Context tags
- Notes
- Condition snapshot if available

## Empty State

Title:

> No wear history yet.

Body:

> Use “Wear now” to start building your wardrobe history.

Action:

- Open closet

---

# 5.10 Backup & Restore

## Purpose

Help users protect local data.

## Mode

Practical / Maintenance

## Content

- Last backup date
- Backup destination
- What is included
- Estimated backup size
- Export backup
- Restore backup
- Import warning

## Restore Warning

Copy:

> Restoring a backup may replace your current wardrobe data. Make a backup first if you want to keep the current version.

Actions:

- Cancel
- Continue restore

---

# 5.11 Settings

## Sections

- Appearance
- Motion
- Privacy
- Backup
- Data management
- Accessibility
- About

## Appearance Controls

```text
Dynamic color:
- On
- Off

Content-based accents:
- Off
- Subtle
- Expressive

Contrast:
- Standard
- Medium
- High
```

## Motion Controls

```text
Motion:
- Follow system
- Subtle
- Expressive
```

---

## 6. Component Specifications

# 6.1 Clothing Card

## Required Content

- Image or placeholder
- Item name
- Category
- Cleanliness chip
- Condition badge if relevant

## Optional Content

- Brand
- Last worn
- Wear count
- Color swatch

## Accessibility Label Example

> Blue Oxford shirt. Clean. Good condition. Last worn 3 days ago. Double tap to view details.

---

# 6.2 Status Chip

## Types

- Cleanliness chip
- Condition chip
- Ownership chip
- Filter chip

## Rules

- Text must always be visible.
- Icon recommended for important statuses.
- Do not rely on color alone.
- Minimum height should support comfortable touch where interactive.

---

# 6.3 Empty State

## Structure

- Illustration or icon
- Short title
- Helpful body
- Primary action
- Optional secondary action

## Tone

Helpful, not blaming.

Good:

> No outfits yet. Combine items from your closet to create your first outfit.

Bad:

> You have not created any outfits.

---

# 6.4 Snackbar

Use for reversible lightweight actions.

Examples:

- Wear recorded
- Item marked dirty
- Laundry marked clean
- Item archived

Snackbars should usually include:

- Message
- Undo action where appropriate

---

# 6.5 Confirmation Dialog

Use for destructive or high-risk actions.

Example:

```text
Delete item?

This removes the item, its photos, and wear history from this device.

Cancel | Delete
```

Prefer archive or retire over delete where possible.

---

## 7. Accessibility Requirements

## 7.1 Touch Targets

- Minimum tappable area: 48dp x 48dp.
- Icon-only buttons require content descriptions.
- Avoid placing small chips too close together.

## 7.2 Text Scaling

Support Android font scaling up to at least 200%.

Rules:

- Cards must expand or reflow.
- Text should not clip.
- Chips should wrap, truncate gracefully, or move into detail views.
- Primary actions should remain reachable.

## 7.3 Screen Reader

Every clothing card should provide meaningful content.

Include:

- Name
- Category
- Cleanliness
- Condition
- Last worn if available
- Action hint

Example:

> Black jeans. Dirty. Needs repair. Last worn yesterday. Double tap to open item details.

## 7.4 Reduced Motion

If reduced motion is enabled:

- Disable expressive avatar flourishes.
- Replace shared transitions with fades.
- Remove pulsing loops.
- Avoid parallax.
- Avoid repeated bounce animations.

## 7.5 Color Independence

Never communicate status through color only.

Use:

- Label
- Icon
- Badge shape
- Position
- Color

---

## 8. Empty, Error, and Edge States

## 8.1 Empty States

| Screen | Empty State |
|---|---|
| Closet | Encourage adding first item |
| Outfits | Encourage creating outfit |
| Laundry | Explain dirty items appear here |
| Repair | Positive empty state |
| History | Explain Wear now creates history |
| Analytics | Explain insights appear after use |

## 8.2 Error States

| Error | UX Response |
|---|---|
| Storage full | Explain issue, preserve form data, suggest freeing space |
| Image missing | Show placeholder, keep metadata visible |
| DB write failed | Show error and keep user input |
| Backup failed | Explain likely cause and allow retry |
| Restore failed | Do not modify current data if restore cannot complete |
| Corrupt backup | Explain file cannot be restored |

## 8.3 Undo Support

Undo should be available for:

- Wear now
- Mark dirty
- Add to laundry
- Mark washed
- Archive item
- Retire item where feasible

Delete may require stronger confirmation instead of relying only on undo.

---

## 9. Compose Implementation Notes

## 9.1 State Hoisting

Screen state should be hoisted into ViewModels.

Recommended pattern:

```kotlin
data class ClosetUiState(
    val isLoading: Boolean = false,
    val items: List<ClothingItemUiModel> = emptyList(),
    val selectedFilters: List<ClosetFilter> = emptyList(),
    val searchQuery: String = "",
    val emptyState: ClosetEmptyState? = null,
    val errorMessage: String? = null
)
```

## 9.2 Local-First UI

Local actions should update immediately through Room-backed state streams.

Avoid:

- blocking spinners for normal local writes;
- network-style loading language;
- confirmation-heavy flows for reversible actions.

Use:

- optimistic local UI updates;
- snackbars with undo;
- clear errors for failed writes.

## 9.3 Design System Layer

Create reusable components:

```text
WardrobeScaffold
WardrobeTopBar
ClothingCard
OutfitCard
StatusChip
ConditionBadge
EmptyState
ErrorState
WearNowButton
LaundryLoadCard
RepairQueueItem
HistoryCalendarCell
AvatarPreviewPanel
```

## 9.4 Motion Abstraction

Define a motion preference model:

```kotlin
enum class MotionPreference {
    FollowSystem,
    Subtle,
    Expressive
}
```

Use this to choose:

- duration
- easing
- transition type
- whether expressive animation is enabled

## 9.5 Theming Abstraction

Define app-level appearance settings:

```kotlin
data class AppearanceSettings(
    val dynamicColorEnabled: Boolean,
    val contentAccents: ContentAccentLevel,
    val contrastLevel: ContrastLevel
)

enum class ContentAccentLevel {
    Off,
    Subtle,
    Expressive
}

enum class ContrastLevel {
    Standard,
    Medium,
    High
}
```

---

## 10. MVP Recommendation

## 10.1 MVP Should Include

- Closet grid
- Add/edit item
- Item detail
- Wear now
- Basic wear history
- Clean/dirty status
- Laundry screen
- Backup/export
- Settings basics

## 10.2 MVP Should Defer

- Full avatar system
- Advanced analytics
- Complex outfit suggestions
- Content-based color extraction
- Highly expressive motion
- Advanced repair workflows

## 10.3 MVP Success Criteria

The MVP is successful if a user can:

1. Add clothing items quickly.
2. See what is clean or dirty.
3. Record what they wore.
4. Review recent wear history.
5. Manage laundry.
6. Back up their local data.
7. Understand that the app is private and local-first.

---

## 11. Final Implementation Guidance

Build the app in this order:

1. Domain state model
2. Design system components
3. Closet and item detail
4. Add/edit item flow
5. Wear now flow
6. History
7. Laundry
8. Backup and restore
9. Settings
10. Outfits
11. Repair queue
12. Avatar and expressive enhancements

Do not start with avatar or advanced motion. Start with the durable habit loop:

```text
Add item → Wear now → History → Laundry → Repeat
```
