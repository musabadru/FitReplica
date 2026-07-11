# Synthesized Themes

## Summary

All sources converge on the same core model: a personal wardrobe app should not copy ecommerce taxonomy directly. It should keep physical classification stable and shallow, then attach flexible metadata for use case, climate, formality, lifecycle, laundry, brand, provenance, and future automation.

The strongest recurring principle is:

- Physical form belongs in taxonomy.
- Context belongs in tags or related metadata.
- Fast filters belong at the top level.
- Detailed, multi-value, import-driven, or avatar-specific information belongs in nested/optional structures.

## Taxonomy

Repeated observations:

- Use one primary physical category path per item.
- Avoid putting use case into the taxonomy. "Formal", "work", "travel", "rainy", and "gym" are contextual tags.
- Keep the hierarchy shallow enough for manual entry, but broad enough to avoid forcing real garments into `Other`.
- Support common wardrobe groups beyond tops/bottoms/shoes: underwear, base layers, sleepwear, loungewear, activewear, swimwear, uniforms, traditional/cultural garments, bags, jewellery, eyewear, and small accessories.
- Keep sets and paired items explicit. A suit, tracksuit, glove pair, shoe pair, co-ord, or matching set may need both a parent item and component items.

Candidate root families:

- Garments
- Footwear
- Bags
- Accessories

Candidate garment families:

- Tops
- Bottoms
- One-piece
- Tailoring
- Outerwear
- Underwear and base layers
- Sleep and lounge
- Active and swim
- Work and uniform
- Traditional and ceremonial

## Colour

Repeated observations:

- Store exact colour data for automation and rendering.
- Use a small semantic palette for user-facing filtering.
- Keep pattern separate from colour.
- Let multi-colour and patterned items hold more than one colour, with a dominant/base colour where useful.
- Perceptual colour spaces are preferred for nearest-colour matching; sources mention Oklch, CIE Lab, and Delta-E style distance.

Canonical palette candidate:

`black, white, grey, navy, blue, light_blue, red, burgundy, pink, green, olive, yellow, beige, tan, brown, orange, purple, gold, silver, multicolour, patterned`

Additional useful colour fields:

- `dominantHex`
- `dominantOklch` or equivalent perceptual value
- `dominantColourId`
- `secondaryColourIds`
- `baseColourId`
- `colourDetailName`
- `colourTreatment`
- `patternType`

## Care

Repeated observations:

- Store care instructions as structured data first and icons second.
- The care model should cover washing, bleaching, drying, ironing, and professional care.
- UI should pair icons with plain-language labels.
- v1 can expose simplified user choices.
- v2 can store full OCR/imported care-symbol detail.
- Care icon artwork may have licensing concerns, so the data model should not depend on copied trademarked symbols.

Core care families:

- Washing
- Bleaching
- Drying
- Ironing
- Professional care

## Fit, Construction, and Avatar Readiness

Repeated observations:

- Fit, silhouette, length, neckline, sleeve length, closure, collar type, rise, leg shape, heel height, and boot shaft height are useful for outfit logic and future avatar mapping.
- Construction fields should not be forced into v1 manual entry.
- Layering role/index matters for outfit generation and avatar rendering.
- Static garment properties should be separated from outfit-specific styling states.

Static garment examples:

- `fitType`
- `silhouette`
- `sleeveLength`
- `neckline`
- `closureType`
- `collarType`
- `rise`
- `legShape`
- `heelHeight`
- `layeringRole`

Outfit-specific styling examples:

- `tuckedIn`
- `sleevesRolled`
- `cuffsRolled`
- `wornOpen`
- `halfZipped`

## Usage and Lifecycle

Repeated observations:

- Personal wardrobe apps need lifecycle fields that retail data does not have.
- Laundry status should affect whether an outfit generator can recommend the item.
- Wear count, last worn date, condition, repair state, archived/retired state, and cost per wear are core management signals.
- Deleting items should not be the primary lifecycle action because it destroys outfit history.

Useful lifecycle states:

- `active`
- `stored`
- `needs_repair`
- `maybe_donate`
- `retired`
- `archived`

Useful laundry states:

- `clean`
- `worn_reusable`
- `dirty`
- `in_laundry`
- `at_cleaners`

## Brand, Provenance, and Authenticity

Repeated observations:

- Brand, manufacturer, label, and country of origin should be separate fields.
- Acquisition/provenance matters for thrifted, gifted, handmade, inherited, swapped, rented, vintage, or bought-new items.
- Authenticity matters for high-value items, replicas, luxury goods, sneakers, and archival pieces.

Useful fields:

- `brandName`
- `subBrand`
- `labelName`
- `manufacturerName`
- `countryOfOrigin`
- `placeOfManufacture`
- `acquisitionType`
- `acquisitionSource`
- `authenticityStatus`
- `purchaseDate`
- `purchasePrice`
- `purchaseCurrency`

## Filter Model

Repeated observations:

- Use quick filters for everyday decisions.
- Put technical filters behind progressive disclosure.
- Include personal-state filters unavailable in ecommerce systems.

Quick filters:

- Category
- Colour
- Laundry status
- Occasion
- Season/weather
- Last worn
- Wear count
- Condition
- Favourite

Advanced filters:

- Material
- Pattern
- Fit
- Construction
- Surface finish
- Brand
- Authenticity
- Source/acquisition
- Weather suitability
- Cost per wear
- Storage location
- Import confidence

## Future Automation

Repeated observations:

- Barcode/GTIN, OCR, and external taxonomies should map into FitReplica's internal IDs, not replace them.
- On-device OCR can later fill material composition, country of origin, care text, and care symbols.
- Image processing can later infer dominant colour, pattern, category, and background-removed thumbnails.
- Avatar support depends on preserving fit, silhouette, construction, and layering metadata early enough.

