# Wardrobe OS — Design Vision

## 1. Product Vision

### 1.1 Product Statement

**Wardrobe OS** is a local-first Android wardrobe management app that turns clothing, outfits, wear history, laundry, repair, and personal styling into a living visual environment.

It should feel less like a spreadsheet and more like a private personal wardrobe world: calm when planning, practical when maintaining clothes, and expressive when exploring outfits.

### 1.2 Core Promise

> Your wardrobe stays private, useful, and alive on your device.

The product should communicate four ideas clearly:

- **Private by default:** no account or server dependency.
- **Fast and local:** every core action works instantly and offline.
- **Visual and personal:** clothing, outfits, and history are represented visually.
- **Emotionally useful:** the app should feel calm, helpful, and occasionally delightful.

---

## 2. UX Principles

### 2.1 Fast Capture First, Detailed Organization Later

Users should be able to add clothing quickly without filling every field.

Required at first:

- Photo or placeholder
- Name
- Category/type

Optional fields can be added later:

- Brand
- Color
- Size
- Season
- Tags
- Notes
- Purchase price
- Condition details

### 2.2 Local-First Confidence

The interface should constantly reinforce that the user is in control.

UX requirements:

- No unnecessary loading states for local actions.
- No account wall before using the app.
- Clear backup and restore screen.
- Clear copy explaining that data is stored locally.
- Failed local actions should preserve user input wherever possible.

### 2.3 Calm by Default, Expressive by Choice

The base app experience should be calm, legible, and stable.

Expressive experiences should appear in:

- Avatar screen
- Outfit exploration
- Saved outfit previews
- Satisfying micro-interactions

Maintenance flows should stay practical and restrained:

- Laundry
- Repair queue
- Backup
- Restore
- Delete
- Storage warnings

### 2.4 Visual Meaning Must Not Depend on Color Alone

Status should always be communicated using a combination of:

- Text
- Icon
- Shape
- Position
- Color

Examples:

- Dirty item: label + icon + chip style
- Needs repair: badge + icon + amber semantic treatment
- Selected filter: checkmark + selected shape + tonal color

---

## 3. Emotional Design Modes

Every major screen should be tagged with one dominant emotional mode.

| Mode | Purpose | Screens | Design Behavior |
|---|---|---|---|
| Calm / Focused | Help the user choose and review clothes clearly | Closet, Item Detail, History | Stable surfaces, gentle motion, medium contrast |
| Playful / Exploratory | Encourage styling and outfit discovery | Avatar, Outfit Builder, Outfit Preview | Richer motion, stronger accents, more expressive transitions |
| Practical / Maintenance | Help the user manage laundry, repair, backup, and errors | Laundry, Repair Queue, Backup, Settings | High clarity, higher contrast, restrained motion |

---

## 4. Core Product Hierarchy

The avatar should be treated as a delight layer, not the foundation of the MVP.

| Priority | Feature Area | UX Role |
|---|---|---|
| P0 | Closet | Core inventory |
| P0 | Item Detail | Core item management |
| P0 | Wear Now | Core habit loop |
| P0 | Laundry Status | Core practical value |
| P1 | Outfit Builder | Planning and reuse |
| P1 | History | Reflection and memory |
| P2 | Avatar | Delight and exploration |
| P2 | Advanced Analytics | Power-user insight |

---

## 5. Primary User Journeys

## 5.1 First Launch

### Goal

Help the user understand the product quickly and take one meaningful action.

### Recommended Flow

1. Welcome
2. Privacy promise
3. First action
4. Optional personalization later

### Screen 1: Welcome

Primary message:

> Your wardrobe, private on your device.

Primary action:

- Get started

Secondary action:

- Restore backup

### Screen 2: Privacy Promise

Explain:

- No account required.
- Works offline.
- Photos and history stay local unless the user exports them.

### Screen 3: First Action

Offer:

- Add first item
- Create first outfit
- Explore demo closet

The demo closet option prevents the first session from feeling empty.

---

## 5.2 Add First Item

### Goal

Let the user capture an item quickly.

### Flow

1. Add photo
2. Add basic details
3. Confirm status
4. Save

### Step 1: Photo

Options:

- Take photo
- Choose from gallery
- Skip photo

### Step 2: Basic Details

Required:

- Name
- Category/type

Optional:

- Brand
- Color
- Size
- Tags

### Step 3: Status

Defaults:

- Cleanliness: Clean
- Condition: Good
- Ownership status: Active

### Step 4: Save

After saving:

- Show the item detail screen, or
- Return to closet with the new card highlighted.

---

## 5.3 Wear Now

### Goal

Make recording a wear event extremely fast.

### Interaction

When the user taps **Wear now**:

1. The app records the wear event immediately.
2. The item or outfit card gives subtle feedback.
3. A snackbar appears.

Snackbar copy:

> Added to today’s wear history.

Actions:

- Undo
- Add context

### Optional Context

The user can add context without blocking the main action:

- Work
- Casual
- Church
- Date
- Travel
- Event
- Custom

---

## 5.4 Laundry Cycle

### Goal

Help users move clothes through cleanliness states.

### Suggested Cleanliness Model

```text
Clean
Worn
Dirty
In laundry
Clean
```

Keep this separate from condition.

Examples:

```text
Clean + Good
Dirty + Good
Clean + Needs repair
Dirty + Needs repair
```

### Laundry Completion

When a user taps **Mark washed**:

1. Items in the load return to Clean.
2. The laundry load shows a completion state.
3. A snackbar offers Undo.

---

## 5.5 Repair Item

### Goal

Make condition tracking useful without making it feel heavy.

### Repair State Flow

```text
Good
Needs repair
In repair
Repaired
Retired
```

### UX Notes

- Repair should surface contextually when there are active repairs.
- The repair queue may live under More when empty.
- If repairs exist, show a card on Closet or Laundry.

Example card:

> 3 items need repair
> Jeans, hoodie, black shoes

Action:

- View repair queue

---

## 5.6 Backup and Restore

### Goal

Give confidence without adding sync complexity.

### Backup Screen Should Explain

- What is included
- Where backup files go
- How restore works
- Whether photos are included
- Last backup date
- Backup file size

### Tone

Use plain, reassuring copy.

Avoid:

> Database export completed.

Better:

> Backup saved. Your wardrobe can be restored from this file later.

---

## 6. Material 3 Theming Direction

## 6.1 Theme Sources

Use three color sources:

1. Wallpaper-derived dynamic color
2. Content-derived local accents
3. Static semantic colors

### Global Theme

Use wallpaper-derived dynamic color for:

- App surfaces
- Navigation
- Primary actions
- Secondary actions
- General components

### Content-Based Accents

Use content-derived accents only inside contained modules:

- Avatar background
- Outfit hero card
- Item detail accent area
- Analytics highlight card

Avoid content-derived colors in:

- Navigation
- Delete actions
- Backup/restore
- Laundry state meaning
- Repair state meaning
- Form validation

### Semantic Colors

Semantic colors should remain stable and readable:

| Meaning | Treatment |
|---|---|
| Error / destructive | Error color role |
| Needs repair | Warm amber semantic badge |
| Clean | Positive status treatment |
| Dirty | Muted warning treatment |
| In laundry | Secondary treatment |

---

## 6.2 Personalization Settings

Expose user controls:

```text
Dynamic color:
- On
- Off

Content-based accents:
- Off
- Subtle
- Expressive

Motion:
- Subtle
- Expressive
- Follow system

Contrast:
- Standard
- Medium
- High
```

Default recommendation:

```text
Dynamic color: On
Content-based accents: Subtle
Motion: Follow system
Contrast: Standard
```

---

## 7. Motion Direction

## 7.1 Motion Schemes

Use two conceptual motion schemes.

### Standard Motion

Used for:

- Closet
- Item detail
- Laundry
- Repair
- History
- Backup
- Settings

Characteristics:

- Fade
- Short slide
- No strong overshoot
- No unnecessary shape morphing

### Expressive Motion

Used for:

- Avatar
- Outfit exploration
- Outfit preview
- Save outfit celebration
- Selected hero interactions

Characteristics:

- Shared element transitions
- Gentle spring motion
- Controlled overshoot
- Card expansion
- Avatar pose changes

---

## 7.2 Reduced Motion

If Android reduced motion or animation removal is enabled:

- Replace shared transitions with fades.
- Disable avatar flourish.
- Avoid pulsing loops.
- Avoid parallax.
- Avoid repeated bounce effects.

---

## 8. Tone and Copy

The app should feel supportive, not judgmental.

Avoid:

> You overuse these sneakers.

Use:

> These sneakers are your most-worn item this month.

Avoid:

> You haven’t worn this jacket.

Use:

> This jacket has not appeared in recent outfits.

Good insight categories:

- Most worn this month
- Not worn recently
- Items to rediscover
- Good cost-per-wear
- Laundry patterns
- Repair backlog

---

## 9. Design Risks

| Risk | Why It Matters | Mitigation |
|---|---|---|
| Avatar dominates MVP | Could delay useful core features | Treat avatar as P2 delight layer |
| Too much dynamic color | Can make image-heavy UI chaotic | Use stable global theme and contained accents |
| Motion overload | Can feel childish or inaccessible | Use expressive motion only in playful contexts |
| Complex laundry states | Can overwhelm casual users | Start with Clean, Worn, Dirty, In laundry |
| No empty-state strategy | First launch may feel dead | Add sample closet and guided first action |
| Local-only anxiety | Users may fear data loss | Make backup/restore prominent and plain-language |

---

## 10. Final Direction

The product should feel like:

- A private wardrobe assistant
- A calm daily planning tool
- A practical laundry and repair tracker
- A visual memory of what the user wears
- A playful outfit exploration space

The app should not feel like:

- A spreadsheet
- A fashion social network
- A heavy inventory system
- A game that forgot practical utility
- A cloud service pretending to be local-first
