# Wardrobe Taxonomy Research Index

## 00 - Brief

This working document organizes research for FitReplica's offline-first digital wardrobe model.

It exists to answer these implementation questions:

- What taxonomy should clothing, footwear, bags, accessories, and edge-case wardrobe items use?
- Which fields belong in the minimal v1 item model?
- Which fields should be optional v2 metadata for OCR, barcode import, analytics, and avatar mapping?
- Which filters are useful in day-to-day wardrobe management?
- How should colour and laundry care be stored for both UI and automation?

Version: 0.1, organized 2026-07-12.

## Table of Contents

- [01 - Raw Research](01-research-raw.md)
- [02 - Synthesized Themes](02-research-synthesis.md)
- [03 - Conflict Notes, Open Questions, and Decisions](03-decisions.md)
- [04 - Canonical Schema v1](04-schema-v1.md)
- [05 - Canonical Schema v2](05-schema-v2.md)

## 01 - Sources

| Source | Date | Contribution | Reliability note |
|---|---:|---|---|
| Source A: cited synthesis | Unknown; organized 2026-07-12 | Standards-aware taxonomy, top-level vs nested field guidance, colour strategy, care model, v1/v2 JSON examples. | Strongest source for external standards framing. Citation tokens are preserved in raw text but need verification before being used as formal references. |
| Source B: Gemini research | Unknown; organized 2026-07-12 | Shallow tree/deep tags framing, global garment examples, mobile/offline schema placement, OCR/barcode/avatar future path. | Useful for architecture and edge cases. Some climate and marketplace examples should be checked before productizing. |
| Source C: shorter sourced answer | Unknown; organized 2026-07-12 | Pragmatic categories, user-facing filters, simplified care fields, implementation handoff language. | Useful for v1 UX and implementation scope. Less rigorous than Source A for standards details. |
| Source D: additional long research | Unknown; organized 2026-07-12 | Wardrobe lifecycle, capsule/audit context, styling-state distinction, fabric behavior, closet status, detailed source list. | Useful for user behavior and lifecycle modeling. Needs source cleanup before citation-quality use. |

## Working Layers

| Layer | File | Rule |
|---|---|---|
| Raw research | [01-research-raw.md](01-research-raw.md) | Preserve original wording and source boundaries. |
| Synthesized notes | [02-research-synthesis.md](02-research-synthesis.md) | Merge repeated ideas by topic. |
| Conflicts and decisions | [03-decisions.md](03-decisions.md) | Record disagreements, open questions, and chosen direction. |
| Final output v1 | [04-schema-v1.md](04-schema-v1.md) | Keep manual-entry schema implementable now. |
| Final output v2 | [05-schema-v2.md](05-schema-v2.md) | Keep expanded schema optional and future-ready. |

## Maintenance Rules

- Add new research to the raw file first.
- Summarize new research into the synthesis file only after reading it end to end.
- Promote a synthesized point into decisions only when it changes the canonical model.
- Keep decisions short, dated, and tied to an implementation consequence.
- Do not duplicate definitions across schema files; define once and link where possible.
- Mark unresolved items as `OPEN`, uncertain items as `REVIEW`, and implementation follow-up as `TODO`.

