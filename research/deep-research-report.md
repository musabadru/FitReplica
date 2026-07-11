# Wardrobe Taxonomy Working Document

Purpose: turn the merged research dump into a usable decision system for FitReplica's wardrobe taxonomy, item metadata, filters, colour model, care model, and future schema work.

Status: working document, not final specification.

Version: 0.1, organized 2026-07-12.

## Table of Contents

- [00 - Brief and Source Log](wardrobe-taxonomy/00-index.md)
- [01 - Raw Research](wardrobe-taxonomy/01-research-raw.md)
- [02 - Synthesized Themes](wardrobe-taxonomy/02-research-synthesis.md)
- [03 - Conflict Notes, Open Questions, and Decisions](wardrobe-taxonomy/03-decisions.md)
- [04 - Canonical Schema v1](wardrobe-taxonomy/04-schema-v1.md)
- [05 - Canonical Schema v2](wardrobe-taxonomy/05-schema-v2.md)

## How to Use This

- If a section answers "what did we learn?", keep it in synthesis.
- If a section answers "what are we using?", keep it in decisions.
- If a section answers "what do we do next?", keep it in open questions.
- Keep raw research untouched unless a new source is added.
- Keep schema files focused on implementable fields, not research commentary.

## Current Canonical Direction

FitReplica should use one stable physical taxonomy path per item, then model occasion, weather, season, formality, lifecycle, laundry state, and styling context as separate axes. The v1 schema should stay small enough for fast manual entry. The v2 schema should add import, OCR, analytics, provenance, and avatar-ready metadata without forcing those fields into the first-run capture flow.

