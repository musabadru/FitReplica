# FitReplica

An offline-first Android wardrobe app: closet management, wear/condition/laundry tracking, outfit building, and analytics — with optional avatar preview and barcode/SKU lookup modules that never compromise the core offline experience.

See [wardrobe_app_architecture.md](wardrobe_app_architecture.md) for the full architecture and build plan, and the [project board](https://github.com/users/musabadru/projects/3) for phased roadmap and issue tracking.

## Tech stack

- Kotlin, Jetpack Compose, Material 3 Expressive
- Multi-module Gradle project with convention plugins (`build-logic/`)
- Room (local persistence), Proto DataStore (preferences), Hilt (DI)
- CameraX + ML Kit (barcode/OCR, opt-in and on-device respectively)

## Module graph

```
:app                    composition root, nav graph, DI wiring
:core:model             pure Kotlin data classes, enums, no Android deps
:core:database           Room entities/DAOs
:core:datastore          Proto DataStore preferences
:core:domain             use-cases, repository interfaces
:core:designsystem       M3 Expressive theme, tokens, shared composables
:core:common             Result wrappers, dispatchers, utils

:feature:closet          grid/list, filters, item detail
:feature:outfit          outfit builder, saved outfits
:feature:history         wear timeline, calendar
:feature:laundry         laundry loads, condition workflow
:feature:analytics       stats, suggestions

:avatar:api              AvatarRenderer interface (NoOp until :avatar:impl-2d ships)
:metadata:api            MetadataProvider interface
:metadata:impl-noop      default no-op implementation
```

## Building

Requires Android Studio (or the SDK + JDK 17 on `PATH`) with SDK platform 35 installed.

```
./gradlew :app:assembleDebug
```

Or open the project root in Android Studio and let Gradle sync, then Run.

## Contributing

- Commits follow [Conventional Commits](https://www.conventionalcommits.org/).
- All changes land via pull request — no direct pushes to `main`.
- See `CLAUDE.md` for the full project conventions.

## License

MIT — see [LICENSE](LICENSE).
