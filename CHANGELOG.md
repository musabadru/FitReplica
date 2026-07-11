# Changelog

## [0.3.0](https://github.com/musabadru/FitReplica/compare/v0.2.0...v0.3.0) (2026-07-11)


### Features

* build out Room v1 schema, FTS4 search, and domain layer for Phase 1 ([cad45cc](https://github.com/musabadru/FitReplica/commit/cad45cc6f5bf82264417d3b284f992ed5a8e1426))
* real :feature:closet implementation with item CRUD, photos, filters, and wear logging ([9e304fb](https://github.com/musabadru/FitReplica/commit/9e304fb3e1277ac2bec1f7c7fdec39b0f5635c28))
* real :feature:closet implementation with item CRUD, photos, filters, and wear logging ([#49](https://github.com/musabadru/FitReplica/issues/49)) ([19c7707](https://github.com/musabadru/FitReplica/commit/19c7707eca1bdb991851c14b19a83362131da432))
* Room v1 schema, FTS4 search, and domain layer (Phase 1 foundation) ([#46](https://github.com/musabadru/FitReplica/issues/46)) ([43ce49a](https://github.com/musabadru/FitReplica/commit/43ce49a51d6be78886e90df15501c8478b872c2c))


### Bug Fixes

* add missing wear_events -&gt; outfits FK and expand cascade test coverage ([5099147](https://github.com/musabadru/FitReplica/commit/50991476bade40c4e1870b649ac35b9b6f06fe57))
* address code review findings on PR [#46](https://github.com/musabadru/FitReplica/issues/46) ([e5f785a](https://github.com/musabadru/FitReplica/commit/e5f785a035fa7a08771ea076548c34cd007eef6c))
* address PR review findings on error handling, photo lifecycle, and filter timing ([5221fda](https://github.com/musabadru/FitReplica/commit/5221fdaa4fd6ce1bc20c4014c61a67065c822342))
* address second round of PR review findings ([6f056d6](https://github.com/musabadru/FitReplica/commit/6f056d6eaebd6fb437e43ab970b1d810f960198e))
* address third round of PR review findings ([1867215](https://github.com/musabadru/FitReplica/commit/18672152945e9fda33b622d22e76f31781442569))
* clear outfitId instead of copying it during wear_events migration ([e748b8a](https://github.com/musabadru/FitReplica/commit/e748b8a712a451c533bde5b25a6a60653967072f))
* scope ImageDao.markPrimary's update to the requested item ([26d984e](https://github.com/musabadru/FitReplica/commit/26d984eb1155ae7710eb24fe396b4ba90765d247))
* scope ImageDao.markPrimary's update to the requested item ([#48](https://github.com/musabadru/FitReplica/issues/48)) ([d233f78](https://github.com/musabadru/FitReplica/commit/d233f78f65447ef465ae324eb6b892dac3dfa130))
* treat punctuation as a search-term separator, not deleted noise ([6679d90](https://github.com/musabadru/FitReplica/commit/6679d90a7e17e5e440b5aa92f379d501f1fd16a8))

## [0.2.0](https://github.com/musabadru/FitReplica/compare/v0.1.0...v0.2.0) (2026-07-11)


### Features

* add avatar and metadata plug-in API modules ([6e9924b](https://github.com/musabadru/FitReplica/commit/6e9924b442eb4d77bda36d10919494531ed9a93d))
* **app:** wire up Hilt application, MainActivity, and nav host ([cc5de5c](https://github.com/musabadru/FitReplica/commit/cc5de5c9be232d8f8b054a83cd459c1a07dbb7f2))
* automate signed APK releases and semver via release-please ([4c3043f](https://github.com/musabadru/FitReplica/commit/4c3043f06f2fe1d3dab72de620ece5eeedaf2a65)), closes [#38](https://github.com/musabadru/FitReplica/issues/38)
* automate signed APK releases and semver via release-please ([#44](https://github.com/musabadru/FitReplica/issues/44)) ([fb86c71](https://github.com/musabadru/FitReplica/commit/fb86c711706507c2e54ed20bcf99d071af635289))
* **core:** add core:database Room setup with schema export ([c24777a](https://github.com/musabadru/FitReplica/commit/c24777aa30af32270ad5da0f4b6922d525cf79da))
* **core:** add core:datastore and core:domain modules ([b4516f6](https://github.com/musabadru/FitReplica/commit/b4516f628e8f0a50b81176bf0a9155fb5462a10b))
* **core:** add core:designsystem theme skeleton ([0c64f6f](https://github.com/musabadru/FitReplica/commit/0c64f6f3a174a94360b168be845add4ca4e5271f))
* **core:** add core:model and core:common modules ([3e78a51](https://github.com/musabadru/FitReplica/commit/3e78a51eed3e1656dd5041da67f425f51852822a))
* Initial Commit ([77ff1cd](https://github.com/musabadru/FitReplica/commit/77ff1cd6f5976b4634e700578e0b183154847282))
* scaffold feature modules ([f72d5de](https://github.com/musabadru/FitReplica/commit/f72d5deb3cda0eb1a9e17d9d3460d03f9956ee87))


### Bug Fixes

* harden release workflow and validate version.properties ([f45e509](https://github.com/musabadru/FitReplica/commit/f45e509b2fe45ee1f2a6b2c5242d1933bb251b97))
* prevent versionCode collisions and fix release-please version marker ([cc91bd2](https://github.com/musabadru/FitReplica/commit/cc91bd2bec26f44843a07afc85d2f9ec26a21e2b))
* resolve build errors found verifying the Phase 0 scaffold ([8cd1a9b](https://github.com/musabadru/FitReplica/commit/8cd1a9b4322d12cc68bf82f42793a5763e3be508))
* resolve CI lint/format failures ([6f53f35](https://github.com/musabadru/FitReplica/commit/6f53f35b7db7acf067cf402b9ccde384be2622b4))
* satisfy ktlint multiline-expression and blank-line rules ([e59b3f3](https://github.com/musabadru/FitReplica/commit/e59b3f3047781cfef7d1f7407fcc3a0ca3f38d16))
* set gradlew executable bit for CI ([04601c3](https://github.com/musabadru/FitReplica/commit/04601c3e85d0c2bed709a55c6dbfffed7a9eecb1))
