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

## Releases

Version numbers are managed automatically by [release-please](https://github.com/googleapis/release-please) from [Conventional Commits](https://www.conventionalcommits.org/) on `main`:

1. Every merge to `main` updates (or opens) a standing "release PR" that accumulates the next semantic version and changelog entries.
2. Merging that release PR creates a git tag and GitHub Release, and bumps `VERSION_NAME` in [`version.properties`](version.properties) — the single source of truth for the app's version. `versionCode` is derived from it at build time (see [`app/build.gradle.kts`](app/build.gradle.kts)), so it never needs manual bumping.
3. That tag triggers [`.github/workflows/release-please.yml`](.github/workflows/release-please.yml) to build a signed release APK and attach it to the GitHub Release.

**Repo secrets required** for signed CI builds (`Settings → Secrets and variables → Actions`): `RELEASE_KEYSTORE_BASE64`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`.

**Building a signed release APK locally**: add these keys to your (gitignored) `local.properties` — `RELEASE_STORE_FILE` (absolute path to the `.jks`), `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD` — then run `./gradlew :app:assembleRelease`. Without these, `assembleRelease` still succeeds but produces an unsigned APK (used by CI's `release-sanity` check on PRs).

## Contributing

- Commits follow [Conventional Commits](https://www.conventionalcommits.org/).
- All changes land via pull request — no direct pushes to `main`.
- See `CLAUDE.md` for the full project conventions.

## License

MIT — see [LICENSE](LICENSE).
