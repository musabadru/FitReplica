# FitReplica

## Git commits

Do not add a `Co-Authored-By: Codex` (or any AI) trailer to commits. All authorship/credit goes solely to Musa Badru <musabadru@gmail.com>.

Commit messages follow [Conventional Commits](https://www.conventionalcommits.org/) (`feat:`, `fix:`, `refactor:`, `docs:`, `chore:`, `test:`, `ci:`, etc., with `!` or a `BREAKING CHANGE:` footer for breaking changes). Version the project with [Semantic Versioning](https://semver.org/) (`MAJOR.MINOR.PATCH`) — bump according to the conventional-commit types accumulated since the last release (breaking → major, `feat` → minor, `fix`/other → patch).

## Branching and pull requests

Never push directly to `main`. All changes land through a pull request from a feature branch, even for small or solo changes — this keeps CI (lint/tests/schema check) as a real gate rather than a formality, and keeps history reviewable.

## Project management (GitHub Issues/Projects/Milestones)

This repo uses GitHub Issues, Milestones (Phase 0–7, per the architecture doc's roadmap), and the "FitReplica Roadmap" Project (`gh project view 3 --owner musabadru`) as the source of truth for planning. Keep this structure in sync with reality as work happens, not just when explicitly asked:

- Finishing work that closes out an issue's acceptance criteria → close the issue (`gh issue close`), referencing the commit/PR.
- Landing a change that a phase's issues describe → check off completed acceptance-criteria checkboxes on the relevant issue(s).
- New work discovered that isn't covered by an existing issue (bug found, scope splits in two, follow-up needed) → create a new issue with appropriate labels (`module:*`, `area:*`, `task`/`epic`) and milestone, rather than letting it go untracked.
- Scope or sequencing changes (a phase's plan shifts, an issue is no longer needed, priorities reorder) → update or close the affected issue(s)/milestone rather than leaving stale entries.
- New issues should be added to the "FitReplica Roadmap" project and assigned to Musa Badru, matching current convention.

When in doubt about whether something needs a tracking update, err toward keeping GitHub current — the whole point of this structure is that it reflects actual project state, not a snapshot from setup day.
