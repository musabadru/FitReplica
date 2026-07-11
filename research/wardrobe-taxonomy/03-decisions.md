# Conflict Notes, Open Questions, and Decisions

## 04 - Conflict Notes

### Formalwear: top-level category or tag?

Some sources list formalwear as a top-level category. Other sources argue that formalwear is a use case and should be modeled as tags or formality metadata.

Decision: formalwear is not a primary taxonomy root. Suits, tuxedo jackets, dress shirts, gowns, and evening dresses remain physical categories. `formal`, `black_tie`, `business_professional`, and similar values belong in formality or occasion metadata.

### Traditional and cultural wear: physical category or context?

Sources agree that global garments should not be buried under `Other`, but they differ on whether traditional wear is a top-level branch or a contextual tag.

Decision: keep `traditional_and_ceremonial` as a garment family in taxonomy when the garment form is culturally specific and not cleanly represented by a generic type. Also allow occasion/context tags such as `ceremonial`, `religious`, or local event names.

### Bags: accessory subtype or separate root?

Some sources place bags under accessories. Others separate bags because they behave more like carried objects than worn adornments.

Decision: model bags as a separate root family. They have different storage, image, outfit, and ownership behavior from jewellery, scarves, and belts.

### Activewear, swimwear, sleepwear, and loungewear

Some sources make these top-level categories. Other sources treat them as functions or contexts.

Decision: model them as physical garment families when the item's construction is specialized enough, but keep `gym`, `home`, `swim`, and similar contexts as tags. Example: `active_and_swim > sports_bra`; `usage.occasionTags = ["gym"]`.

### Colour matching: Oklch or CIE Lab?

Sources mention both Oklch and CIE Lab/Delta-E for perceptual distance.

Decision: schema should not hard-code the algorithm. Store exact colour and a semantic colour id. Add `dominantOklch` as the preferred v2 field because it is useful for modern UI work, but leave room for implementation to compute with any validated perceptual distance approach.

### Laundry status versus condition status

Some sources group clean/dirty/repair/retired into one status field. Others separate laundry and lifecycle.

Decision: separate them. `laundryStatus` answers "can this be worn today?" `conditionStatus` and `closetStatus` answer "what is its physical or ownership state?"

### Static garment facts versus outfit styling state

One source emphasizes that states like tucked, rolled sleeves, and half-zipped are not item properties.

Decision: keep styling states out of the item schema. They belong to outfit records or outfit item overrides.

## 05 - Open Questions

- OPEN: Should FitReplica support both a parent set item and separately wearable child items in v1, or defer set relationships to v2?
- OPEN: Should current `ClothingType` enum values be expanded gradually, or replaced with a versioned taxonomy table?
- OPEN: How much traditional/regional vocabulary should ship in the first taxonomy bundle versus being user-extensible?
- OPEN: Should colour family labels use `grey` or `gray` in code? Current research uses both. Pick one canonical spelling before enum work.
- OPEN: Should `worn_reusable` be included in v1 laundry status? It improves real wardrobe tracking but adds one more choice during capture.
- OPEN: Should size fields live in v1? Current app models size separately, but the research did not make size central to the first capture flow.
- REVIEW: Verify care-symbol trademark/licensing details before rendering ISO-like symbols in product UI.
- REVIEW: Verify external taxonomy references before using them as citations in public documentation.

## 06 - Decisions

### D001 - Taxonomy Uses Physical Form

Date: 2026-07-12.

Use a versioned physical taxonomy for item classification. Do not classify by occasion, weather, season, or formality.

Implementation consequence: category IDs should represent physical form, while use case values are stored as tags or metadata arrays.

### D002 - One Primary Category Path

Date: 2026-07-12.

Each item has exactly one primary category path.

Implementation consequence: avoid multiple primary categories for one item. Use relationships, tags, and outfit records for overlap.

### D003 - Context Is Orthogonal Metadata

Date: 2026-07-12.

Season, weather, occasion, activity, formality, and compatibility are independent metadata axes.

Implementation consequence: do not encode values like `work_shirt`, `winter_coat`, or `formal_shoes` as primary categories unless they describe physical form.

### D004 - V1 Prioritizes Fast Manual Entry

Date: 2026-07-12.

V1 should require only high-value fields users can provide quickly: photo, category, type, colour, laundry status, condition, and basic tags.

Implementation consequence: detailed provenance, care symbols, material percentages, construction, and avatar hints are optional.

### D005 - V2 Adds Depth Without Breaking V1

Date: 2026-07-12.

V2 fields should extend the item record through optional nested structures and related tables.

Implementation consequence: do not make advanced fields required for old records.

### D006 - Colour Has Two Layers

Date: 2026-07-12.

Store exact colour values for automation and semantic colour IDs for filtering.

Implementation consequence: the UI filters on semantic palette values, not raw hex names or large colour dictionaries.

### D007 - Care Is Structured Data Plus Plain Text

Date: 2026-07-12.

Care instructions should be stored as structured values and displayed with plain-language labels.

Implementation consequence: icons are presentation details, not the source of truth.

### D008 - Lifecycle Is Not Deletion

Date: 2026-07-12.

Retired, donated, archived, repair, and stored items should remain in history unless the user explicitly deletes them.

Implementation consequence: outfit history and cost-per-wear analytics remain stable.

