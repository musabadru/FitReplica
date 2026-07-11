# Wardrobe Taxonomy and Attribute Model for an Offline First Wardrobe App

## Design principles

The most robust model for a personal wardrobe app is a **three-layer structure**: a canonical wardrobe taxonomy you control, a practical attribute model for manual entry and filtering, and an external mapping layer for barcode, OCR, marketplace, and avatar use later. That approach follows how Google expects one specific category per product, how Shopify separates a product category from structured attributes such as neckline and sleeve length, how GS1 and Schema.org model standard product properties, and how modern fashion datasets separate category from fine-grained visual attributes and landmarks. citeturn31view0turn36view0turn18view0turn19view0turn17search1turn16search18

For a wardrobe app, the key design decision is this: **use category for physical form, not use case**. A shirt is still a shirt whether the owner wears it casually, formally, or for travel. Google explicitly recommends a single category based on the product’s main function and the most specific category possible, while Shopify’s taxonomy uses one product category plus extra attributes and metafields. In practice, that means “shirt” or “boots” should be taxonomy, while “formal”, “travel”, “church”, “gym”, and “rainy season” should be tags or metadata axes. citeturn31view0turn36view0

Because external taxonomies keep changing, your internal model should be **stable and versioned**, with mapping tables rather than hard-coded dependence on any single retail taxonomy. Google says its taxonomy is continuously evolving, and Shopify’s current releases still add thousands of categories and attributes. That is a strong argument for keeping your own canonical IDs and storing external references alongside them. citeturn31view0turn24view1

## Recommended taxonomy hierarchy

The cleanest structure is a **dual-axis model**:

- **Axis A: physical taxonomy** — exactly one primary category path per item.
- **Axis B: contextual metadata** — season, occasion, weather, formality, activity, compatibility, laundry status.

That design keeps entry simple, supports filtering well, and stays compatible with future import pipelines. It also mirrors the way Google, Shopify, Schema.org, and fashion-vision datasets separate category from attributes. citeturn31view0turn36view0turn19view0turn17search1turn16search0

| Root | Family | Recommended leaf examples |
|---|---|---|
| Garments | Tops | T-shirt, polo shirt, button-down shirt, dress shirt, blouse, tank top, camisole, crop top, bodysuit, sweater, cardigan, hoodie, sweatshirt |
| Garments | Bottoms | Trousers, jeans, chinos, joggers, cargo trousers, leggings, shorts, skirt, culottes, overalls |
| Garments | One-piece | Dress, jumpsuit, romper, playsuit, dungarees |
| Garments | Tailoring | Blazer, suit jacket, suit trousers, tuxedo jacket, tuxedo trousers, waistcoat |
| Garments | Outerwear | Jacket, coat, trench coat, parka, rain jacket, poncho, liner, insulator |
| Garments | Underwear and base layers | Briefs, boxers, bra, bralette, undershirt, shapewear, thermal top, thermal bottom |
| Garments | Sleep and lounge | Pyjama top, pyjama bottoms, robe, dressing gown, lounge trousers, lounge set |
| Garments | Active and swim | Sports bra, running top, running shorts, gym leggings, swimsuit, bikini, swim shorts, rash guard |
| Garments | Work and uniform | Scrubs, chef coat, coveralls, school uniform blouse, security jacket |
| Garments | Traditional and ceremonial | Abaya, jilbab, kaftan, kimono, yukata, sari, lehenga, cheongsam, ceremonial dress |
| Footwear | Casual and dress shoes | Trainers, loafers, derby shoes, oxfords, flats, heels, sandals, slippers |
| Footwear | Boots | Ankle boots, Chelsea boots, combat boots, hiking boots, knee boots |
| Bags | Carry goods | Backpack, tote, handbag, shoulder bag, crossbody bag, clutch, briefcase, duffel |
| Accessories | Wearable accessories | Belt, scarf, hat, cap, gloves, tie, bow tie, pocket square, hair accessory |
| Accessories | Hosiery and small goods | Socks, tights, stockings, leg warmers, wallet, card holder |
| Accessories | Jewellery and eyewear | Watch, bracelet, necklace, earrings, ring, sunglasses, prescription frames |

This hierarchy is intentionally broader than many wardrobe apps because day-to-day wardrobes routinely include specialist items that common “tops / bottoms / shoes” schemes miss. Shopify’s current taxonomy releases have expanded into areas such as liners and insulators, ponchos, school uniforms, military uniforms, socks subtypes, period swimwear, and traditional and ceremonial clothing; the FTC’s textile guidance also calls out everyday categories such as scarves and hosiery-related special cases. citeturn24view1turn32view0

The **usage axis** should be separate:

| Metadata axis | Suggested values |
|---|---|
| Style / function | Casual, formal, smart casual, athletic, workwear, uniform, ceremonial, homewear, travel |
| Occasion | Work, date, wedding, church, party, gym, travel, home, interview |
| Season | Summer, winter, rainy season, transitional |
| Weather | Hot, cold, rainy, dry, humid, indoor, outdoor |

This split avoids duplication. A blazer stays under `Garments > Tailoring > Blazer`, while its usage tags can include `work`, `formal`, `travel`, and `transitional`. That is more consistent with “one most specific category” guidance and with retail taxonomies that keep attributes such as colour, fabric, target gender, neckline, and sleeve length outside the category tree itself. citeturn31view0turn36view0

## Field model and data structure

For an offline-first app, only **high-frequency sort and filter fields** should sit at the top level. Detailed or repeated structures should be nested. A good rule is: if it is shown in list views, used in quick filters, or changed very often, keep it top-level; if it is complex, multi-valued, or mostly for detail pages or imports, nest it. That lines up with Google’s product-level category and identifiers, Shopify’s category plus metafields, and Schema.org’s distinction between direct properties and richer grouped structures. citeturn31view0turn36view0turn19view0turn35view0

| Placement | Keep here | Why |
|---|---|---|
| Top-level | `id`, `primaryCategoryId`, `subcategoryId`, `itemKind`, `name`, `dominantColourId`, `laundryStatus`, `conditionStatus`, `lastWornAt`, `wearCount`, `isFavourite`, `isArchived` | Fast list rendering, fast local filtering, simple sync conflict handling |
| Nested | `visual`, `materials`, `care`, `brand`, `authenticity`, `usage`, `fit`, `construction`, `measurements`, `externalMappings`, `attachments` | Multi-value, more detailed, easier to extend without breaking v1 |

### Recommended grouped field schema

| Group | Core fields | Good v1 fields | Good v2 fields |
|---|---|---|---|
| Identity fields | Stable identity and classification | `id`, `itemKind`, `primaryCategoryId`, `subcategoryId`, `name`, `notes` | `categoryPath`, `taxonomyVersion`, `aliases`, `setMembership`, `pairedItemIds`, `storageLocation`, `ownerProfile` |
| Visual fields | What the item looks like | `dominantColourId`, `secondaryColourIds`, `patternType`, `photoIds` | `colourPalette[]`, `baseColourId`, `accentColourIds`, `surfaceFinish`, `printType`, `graphicText`, `avatarHints` |
| Material and care fields | Fibre content and cleaning | `materialSummary`, `careSummary`, `delicateCare` | `materials[]` with percentages, `fabricType`, `fabricWeight`, `stretchLevel`, `breathability`, `insulation`, `wrinkleResistance`, `waterResistance`, full `care{}` object |
| Brand and authenticity fields | Provenance | `brandName`, `authenticityStatus` | `subBrand`, `labelName`, `manufacturerName`, `countryOfOrigin`, `placeOfManufacture`, `acquisitionSource`, `giftedBy`, `thrifted`, `handmade`, `replicaReason` |
| Usage and history fields | How the owner uses the item | `laundryStatus`, `lastWornAt`, `wearCount`, `occasionTags`, `seasonTags` | `wearLog[]`, `cost`, `costPerWear`, `purchaseDate`, `retireDate`, `repairStatus`, `outfitCompatibilityTags`, `usageScore`, `frequencyBand` |
| Fit and construction fields | Shape, silhouette, details | `fitType`, `sleeveLength`, `neckline`, `closureType` | `silhouette`, `rise`, `legShape`, `hemLength`, `collarType`, `lapelType`, `pocketType`, `seamType`, `embellishments[]`, `heelHeight`, `bootShaftHeight` |

This grouping is a practical synthesis of what current retail standards expose for discovery and what computer-vision fashion datasets annotate for finer recognition. Shopify already treats fields such as size, neckline, sleeve length, top length, fabric, target gender, clothing features, and colour as structured category attributes, while Fashionpedia and DeepFashion organise attributes into category, part, fabric, shape, texture, style, and landmark-like structures. citeturn36view0turn17search1turn17search2turn16search0turn16search18

### Fields that are especially useful for wardrobe filtering and organisation

The most valuable fields for a personal wardrobe are not always the most detailed ones. For real use, the strongest filters are usually **category, colour, laundry state, season/weather, occasion, material, fit, pattern, last worn, wear count, and repair/retired state**. Those are the attributes that users can recall easily and that map directly to wardrobe tasks such as “what can I wear today?”, “what needs washing?”, and “what have I not worn in months?”. Their retail equivalents also appear repeatedly in standard taxonomies and feeds, especially category, colour, material, brand, size, gender, and GTIN. citeturn31view0turn20view0turn20view1turn20view2turn36view0

A useful rule for **top-level vs nested** is:

- **Top-level**: identity, category, dominant colour, current state, last-worn signal.
- **Nested**: anything multi-value or technical, including material percentages, full care instructions, construction, detailed colour palettes, import metadata, and avatar hints.

That balance keeps v1 simple while making v2 extensible. citeturn35view0turn19view0

## Filter model for the app UI

The best UI filter set has two layers: **quick filters** for everyday use, and **advanced filters** for deeper management. This mirrors how structured commerce taxonomies expose high-value discovery fields, but the wardrobe app should add personal-state filters that marketplaces do not usually have, such as clean/dirty and last worn. citeturn36view0turn20view0turn24view0

### Quick filters

| Filter | Control | Why it belongs in v1 |
|---|---|---|
| Category | Chips / picker | Primary browse mechanism |
| Colour | Chips / swatches | High recall for users |
| Laundry status | Chips | Essential for wardrobe management |
| Occasion | Chips | Essential for outfit selection |
| Season / weather | Chips | Practical day-to-day use |
| Last worn | Sort / preset ranges | Key for rotation |
| Wear count | Sort / ranges | Helps identify favourites and neglected items |
| Condition status | Chips | Repair / retire workflows |
| Favourite | Toggle | Fast retrieval |
| Item completeness | Chips | Single item / pair / set |

### Advanced filters

| Filter | Suggested values |
|---|---|
| Material | Cotton, wool, denim, leather, linen, polyester, etc. |
| Pattern | Solid, striped, plaid, floral, graphic, camo, abstract |
| Fit | Slim, regular, relaxed, oversized, tapered, straight |
| Construction | Hooded, collared, lapel, zip, button, lace-up |
| Surface finish | Matte, glossy, ribbed, washed, distressed, embroidered |
| Authenticity | Original, replica, gifted, thrifted, handmade, unknown |
| Brand | Brand picker |
| Weather suitability | Hot, cold, rainy, humid, indoor, outdoor |
| Outfit compatibility | Custom tags |
| Cost per wear | Numeric range |
| Source / acquisition | Bought new, thrifted, gifted, handmade |
| Storage location | Drawer, rail, shelf, shoe rack |
| Import confidence | Useful later for OCR / barcode workflows |

In practice, the most successful approach is to keep **technical fields hidden until needed**. Most users should never have to enter seam type or detailed professional care solvent codes unless they want to. Offline-first manual entry works best when the first screen is short and the detail screen is optional. That is also consistent with Shopify’s model of a standard category plus selectively added metafields rather than forcing every attribute for every item. citeturn36view0

## Colour strategy for wardrobe items

For wardrobe use, the right answer is **not** to expose dozens or hundreds of colour names as primary filters. Instead, store both a precise machine-readable colour and a small semantic wardrobe palette. The semantic palette should drive filtering; the exact swatch should support later import, photo extraction, and avatar rendering. W3C’s CSS colour specifications and MDN’s named-colour support are useful as baseline references, while perceptual spaces such as Oklab/OkLCh are better for nearest-name matching because they separate lightness, chroma, and hue more cleanly than raw RGB naming. citeturn28view0turn28view1turn28view2turn28view3

### Recommended storage pattern

| Field | Purpose |
|---|---|
| `dominantHex` | Original captured swatch |
| `dominantOklch` | Perceptual matching and clustering |
| `semanticColourId` | Main filter bucket |
| `secondaryColours[]` | Accent colours |
| `baseColourId` | Ground colour for patterned items |
| `patternType` | Keeps pattern separate from colour |
| `colourDetailName` | Optional shade label such as “graphite” or “oxblood” |

### Recommended user-facing semantic palette

Use a deliberately small palette such as:

`black, white, grey, navy, blue, light blue, red, burgundy, pink, green, olive, yellow, beige, tan, brown, orange, purple, gold, silver, multicolour, patterned`

That palette is much more usable for filter chips than the large CSS named-colour set. If you want finer descriptive names, make them **secondary labels** only. Shopify explicitly supports customising colour entries such as renaming `black` to `graphite`, which is a good model here: keep the canonical bucket stable, let the display label be friendlier if needed. citeturn28view0turn28view3turn36view0

### Recommended mapping strategy

A good offline mapping pipeline is:

1. Convert any picked or imported colour to **Oklch**.
2. Match to the nearest **semantic palette anchor**.
3. If the item has multiple strong colours, keep up to three colours with rough percentages.
4. If no single colour dominates, classify as `multicolour`.
5. If the item is defined mainly by a surface design, set `patternType` and also keep a `baseColourId`.

That gives you readable filters while preserving exact values for later automation. Fashion-oriented colour systems such as Pantone Fashion, Home + Interiors are helpful for manufacturing-level precision, but they are too granular for primary filtering in a wardrobe UI. They fit better as an import or reference layer. citeturn28view1turn28view2turn27search2turn27search5

### Suggested colour library choices

For an offline-first app, the most practical stack is:

- **Primary user-facing layer**: your own curated wardrobe palette.
- **Fallback name source**: CSS named colours for broad recognisable labels. citeturn28view0turn28view3
- **Optional detailed fashion reference**: Pantone FHI for future import or premium tooling. citeturn27search2turn27search5
- **Optional developer libraries**: libraries such as `color-namer` can generate nearest colour names offline and use distance-based matching, but the result should still be mapped back into your smaller wardrobe palette rather than exposed directly to end users. citeturn27search16turn27search0

## Laundry and care representation

The app should represent care instructions as **structured data first, icons second**. ISO 3758 and GINETEX organise care in a fixed order: washing, bleaching, drying, ironing, and professional care. GINETEX also explains the key visual grammar: the tub for washing, triangle for bleaching, square for drying, iron for ironing, and circle for professional cleaning; bars indicate milder cycles, dots indicate temperature severity, and a cross means the treatment is not allowed. citeturn21view0turn6search8turn33view0turn33view1turn33view2

### Recommended care data model

| Care family | Structured fields | Example values |
|---|---|---|
| Washing | `allowed`, `method`, `maxTempC`, `cycle` | `true`, `machine`, `30`, `mild` |
| Bleaching | `allowed`, `bleachType` | `false`, `none`; or `true`, `oxygen_only` |
| Drying | `tumbleAllowed`, `tumbleHeat`, `naturalDryMethod`, `shade` | `false`, `none`, `flat`, `true` |
| Ironing | `allowed`, `heatLevel`, `maxTempC`, `steamAllowed` | `true`, `2`, `150`, `false` |
| Professional care | `allowed`, `method`, `solventCode`, `process` | `true`, `dry_clean`, `P`, `mild` |

### Recommended UI pattern

Display each item’s care in three layers:

| Layer | What the user sees |
|---|---|
| Fast summary | Icon row plus plain language, for example “Wash 30°C, no bleach, line dry” |
| Detail sheet | Full labels with explanations such as “mild cycle” or “dry flat in shade” |
| Structured data | Encoded enums for sorting, filtering, and import |

This is better than icon-only UI because many users recognise some care symbols but not all of them instantly, while plain-language text improves accessibility and reduces mistakes. FTC guidance similarly emphasises that garments need at least one safe cleaning method and that washing, drying, ironing, bleaching, and warnings should be clear. citeturn6search0turn21view0turn33view0turn33view1turn33view2

### Care symbol notes that matter for implementation

A few details are especially important:

- Washing numbers indicate maximum wash temperature in degrees Celsius. One bar means milder treatment; two bars mean very mild treatment. citeturn21view0
- The bleaching triangle distinguishes any bleach, oxygen-only bleach, and no bleach. citeturn33view0
- Drying covers both tumble drying and natural drying methods such as line dry, drip dry, flat dry, and shade variants. citeturn33view1
- Iron dots indicate temperature range; users should follow the label rather than infer settings from fibre alone. citeturn33view2
- Professional cleaning letters inside the circle are mainly for professional cleaners, not most consumers. citeturn33view3

One implementation caution is easy to miss: GINETEX states that the care symbols are protected trademarks in many countries and may not be reproduced or used without a licence. That means your data model should be independent of the icon artwork. Store neutral care codes in the database, then render either licensed symbols or non-identical in-app icons plus text. citeturn33view3

## Minimal and expanded schemas

A good v1 should support **fast manual entry** in under a minute. A good v2 should add richer provenance, care, and avatar-related detail without breaking old data. The split below follows the standards above: one stable category, a few high-value discovery attributes, and optional external identifiers and attribute families for future import. citeturn31view0turn36view0turn19view0turn20view0turn20view2

### Minimal v1 schema

```json
{
  "id": "uuid",
  "itemKind": "garment | footwear | bag | accessory",
  "primaryCategoryId": "tops",
  "subcategoryId": "button_down_shirt",
  "name": "Blue striped office shirt",
  "photoIds": ["photo_1"],
  "dominantColourId": "blue",
  "secondaryColourIds": ["white"],
  "patternType": "striped",
  "materialSummary": "cotton blend",
  "seasonTags": ["transitional"],
  "occasionTags": ["work", "casual"],
  "laundryStatus": "clean | dirty | in_laundry",
  "careSummary": "wash_30_line_dry_low_iron",
  "fitType": "regular",
  "brandName": "optional",
  "authenticityStatus": "original | thrifted | gifted | handmade | unknown",
  "lastWornAt": "date | null",
  "wearCount": 0,
  "conditionStatus": "good | needs_repair | torn | retired",
  "isFavourite": false,
  "notes": "optional"
}
```

### Expanded v2 schema

```json
{
  "id": "uuid",
  "taxonomyVersion": "wardrobe-taxonomy-1",
  "itemKind": "garment",
  "categoryPath": ["garments", "tops", "shirts", "button_down_shirts"],
  "name": "Blue striped office shirt",
  "aliases": ["work shirt"],
  "photos": [{ "id": "photo_1", "role": "front" }],

  "visual": {
    "dominantHex": "#4E6FAE",
    "dominantOklch": { "l": 0.62, "c": 0.09, "h": 255.0 },
    "semanticColourId": "blue",
    "secondaryColours": ["white"],
    "baseColourId": "blue",
    "patternType": "striped",
    "surfaceFinish": "woven",
    "graphicText": null
  },

  "materials": {
    "fabricType": "poplin",
    "composition": [
      { "materialId": "cotton", "percent": 70 },
      { "materialId": "polyester", "percent": 27 },
      { "materialId": "elastane", "percent": 3 }
    ],
    "properties": {
      "stretchLevel": "low",
      "breathability": "medium",
      "insulation": "low",
      "wrinkleResistance": "medium",
      "waterResistance": "none",
      "delicateCare": false
    }
  },

  "care": {
    "washing": { "allowed": true, "method": "machine", "maxTempC": 30, "cycle": "mild" },
    "bleaching": { "allowed": false, "bleachType": "none" },
    "drying": { "tumbleAllowed": false, "naturalDryMethod": "line", "shade": false },
    "ironing": { "allowed": true, "heatLevel": 2, "maxTempC": 150, "steamAllowed": true },
    "professionalCare": { "allowed": false, "method": "none", "solventCode": null, "process": null }
  },

  "brand": {
    "brandName": "Example Brand",
    "subBrand": null,
    "labelName": null,
    "manufacturerName": "Example Manufacturing Ltd",
    "countryOfOrigin": "TR",
    "placeOfManufacture": "Izmir, Turkey"
  },

  "authenticity": {
    "status": "original",
    "acquisitionType": "bought_new",
    "acquisitionSource": "retail",
    "giftedBy": null,
    "thrifted": false,
    "handmade": false
  },

  "usage": {
    "laundryStatus": "clean",
    "seasonTags": ["transitional"],
    "weatherTags": ["indoor", "dry"],
    "occasionTags": ["work", "casual"],
    "formality": "smart_casual",
    "lastWornAt": null,
    "wearCount": 0,
    "wearLog": [],
    "cost": 32.0,
    "costCurrency": "GBP",
    "costPerWear": null,
    "compatibilityTags": ["office", "layering"]
  },

  "fit": {
    "fitType": "regular",
    "silhouette": "straight",
    "sleeveLength": "long",
    "neckline": "collared",
    "topLength": "regular"
  },

  "construction": {
    "collarType": "button_down",
    "closureType": "buttoned",
    "pocketType": "chest",
    "seamDetails": ["double_stitched"],
    "embellishments": []
  },

  "relationships": {
    "setMembership": null,
    "pairedItemIds": [],
    "outfitTemplateIds": []
  },

  "externalMappings": {
    "gtin": null,
    "mpn": null,
    "sku": null,
    "googleProductCategory": null,
    "shopifyCategoryId": null,
    "schemaOrgType": "Product",
    "sourcePayloadRef": null,
    "importConfidence": null
  },

  "status": {
    "conditionStatus": "good",
    "repairStatus": "none",
    "isFavourite": false,
    "isArchived": false
  }
}
```

The v2 shape above leaves room for the metadata external systems commonly provide: brand, GTIN, MPN, country of origin, material, colour, category, and variant signals such as size and gender. Google requires or strongly recommends many of those identifiers and attributes for product classification, and Schema.org/GS1 define corresponding properties for machine-readable interchange. citeturn20view0turn20view1turn20view2turn19view0turn19view1turn19view2turn35view0

## Missing categories and future-proofing

Several categories are often missing from simple wardrobe schemas but matter in real life: **socks and hosiery, swimwear, shapewear, thermals/base layers, loungewear sets, rainwear, ponchos, liners/insulators, uniforms, maternity/nursing, and traditional or ceremonial garments**. Shopify’s current taxonomy work is especially useful here because it has recently added more detail in socks, swimwear, liners and insulators, tuxedo parts, school and military uniforms, and traditional clothing branches such as abayas, jilbabs, saris, lehangas, cheongsams, kimonos, and yukatas. citeturn24view1

A few additional modelling choices will make the schema flexible enough for future barcode, OCR, and avatar work:

First, keep **one canonical item record** plus an `externalMappings` object. Retail taxonomies vary across stores and over time, and academic work on fashion categorisation explicitly notes that cross-store taxonomy variation is a real problem. So your app should store your own category IDs and treat Google, Shopify, GS1, and import-source values as mappings, not as your master truth. citeturn23academia21turn31view0turn24view0

Second, treat **brand, manufacturer, and country of origin as different fields**. Schema.org distinguishes `brand`, `manufacturer`, and `countryOfOrigin`, and FTC textile guidance also separates fibre content, country of origin, and the identity of the manufacturer or other responsible business. In user terms: brand is the label the wearer recognises; manufacturer is who made it; country of origin is where the product originates under the relevant rules. citeturn19view0turn19view1turn19view2turn32view0

Third, do **not** make gender a primary taxonomy branch. Google and Shopify both treat gender as an attribute alongside size, size type, fabric, sleeve length, neckline, and colour. For a personal wardrobe, gender is best stored as optional metadata such as `marketedGender` or `sizeProfile`, because users often own items across “menswear”, “womenswear”, and “unisex” labels. citeturn36view0turn36view1turn36view2

Fourth, reserve an `avatarHints` or `fit` layer for future visual applications. Fashionpedia’s ontology includes apparel categories, apparel parts, and hundreds of attributes; DeepFashion groups attributes into texture, fabric, shape, part, and style; DeepFashion2 adds dense landmarks and detailed geometry per garment. In practice, the wardrobe fields most worth preserving for avatar mapping are **neckline, sleeve length, top length, hem, rise, leg shape, collar type, closure type, silhouette, heel height, and boot shaft height**. citeturn17search1turn17search2turn16search0turn16search18

### Final recommendation

If you want one concise implementation rule set, it is this:

- Keep **exactly one primary physical category path** per item.
- Keep **usage, season, weather, and occasion** as orthogonal tags.
- Keep **fast-filter fields top-level** and everything complex in nested groups.
- Store **precise colour values plus a small semantic colour palette**.
- Store **care instructions as structured codes plus plain-language UI labels**.
- Use **generic fibre names** for materials, not trade names, to stay import-friendly.
- Add **external IDs and taxonomy mappings** without letting them control your internal model.
- Make v1 short enough for manual entry; make v2 rich enough for import, analytics, and avatar features. citeturn31view0turn36view0turn32view0turn19view0turn24view1turn17search1




GEMINI RESEARCH

Comprehensive Architecture and Taxonomy Model for Offline-First Digital Wardrobe SystemsIntroductionThe architectural design of a digital wardrobe management system requires a fundamental paradigm shift away from traditional e-commerce data structures. E-commerce taxonomies, such as the Google Product Taxonomy, are expressly optimized for multi-merchant discoverability, hierarchical search engine optimization, and broad consumer navigation across vast catalogs. These structures typically employ deep, rigid trees—for example, categorizing a simple garment through a heavily nested path like Apparel & Accessories > Clothing > Outerwear > Coats & Jackets > Trench Coats. While this rigidity serves global inventory management, it introduces severe cognitive friction for an individual attempting to quickly digitize and manage their personal closet.When a user interacts with a personal digital wardrobe app—such as Stylebook, Whering, or Acloset—their mental model is rooted in spatial memory, daily utility, situational function, and outfit coordination, rather than retail categorization. Consequently, the data model must be engineered to support immediate, low-friction manual data entry while simultaneously laying a robust foundation for advanced, future-facing features. These features include Optical Character Recognition (OCR) for reading care labels, barcode and Universal Product Code (UPC) scanning for automated metadata ingestion, and sophisticated topological mapping for 3D avatar virtual try-on environments.Furthermore, the strict requirement for an offline-first architecture dictates that the underlying database—typically a lightweight embedded relational database such as SQLite or Realm deployed on mobile hardware—must process complex filtering, outfit generation, and analytics without relying on continuous cloud synchronization. This constraint necessitates a highly normalized schema that carefully delineates between top-level indexed attributes required for rapid querying and nested data payloads reserved for detailed analytics or machine learning (ML) tasks. This report provides an exhaustive blueprint for constructing this system, detailing the optimal clothing taxonomy, an expansive attribute schema, a functional UI filter engineering strategy, a mathematically sound color-mapping protocol, and an ISO-compliant laundry care model.The Architecture of Clothing TaxonomyThe primary failure point of deploying standard retail taxonomies in personal wardrobe applications is the over-reliance on deep hierarchical trees. Deep trees frequently lead to user fatigue during manual entry and induce classification hallucinations in automated models, as items often traverse strict boundaries. For example, matching two-piece sets, commonly referred to as "Co-ords," exist simultaneously as tops and bottoms, defying single-node hierarchical placement.Additionally, Western-centric taxonomies consistently fail to capture critical global garments essential for a culturally comprehensive application. Garments such as the East African Gomesi (a structured, brightly colored floor-length dress tied with a sash), the Kanzu (a tunic-like robe worn by men across the African Great Lakes region), the Mushanana (a regional skirt and shawl combination), or the West African Boubou are often entirely omitted or improperly relegated to obscure "Costumes" sub-nodes in traditional structures. A globally viable wardrobe architecture must elevate these garments to primary or secondary hierarchical levels.To resolve these structural deficiencies, the recommended architectural approach employs a "Shallow Tree, Deep Tags" methodology. In this model, the primary taxonomy tree is restricted to a maximum depth of two levels, dedicated exclusively to the structural nature of the garment (where and how it sits on the human body). All functional, situational, and weather-dependent contexts are completely decoupled from the hierarchy and applied as cross-cutting tags.Structural Classification ModelThe structural classification dictates the physical rendering of the garment in the user interface and its base compatibility in the outfit generation engine. The primary category defines the body zone, while the subcategory provides the specific shape descriptor.Primary CategoryDefinition & Coverage ZoneExample Subcategories (Shape Descriptors)TopsUpper body coverage; includes skin-contact base layers and mid-layers.T-Shirt, Tank Top, Camisole, Blouse, Button-Down, Collared Shirt, Polo, Sweater, Sweatshirt, Hoodie, Crop Top.BottomsLower body coverage from the waist down.Jeans, Trousers, Chinos, Joggers, Cargo Pants, Leggings, Shorts, Skirts.One-Piece / Full BodyContinuous garments covering both upper and lower body zones.Dress, Jumpsuit, Romper, Overalls, Gomesi, Kanzu, Boubou.OuterwearWeather-facing outer layers designed for external exposure.Jacket, Coat, Blazer, Cardigan, Trench Coat, Raincoat, Puffer, Vest, Poncho, Koti.FootwearProtective and stylistic coverings for the feet.Sneakers, Loafers, Boots, Sandals, Heels, Flats, Wedges, Slippers, Oxfords.AccessoriesSupplementary items augmenting the primary outfit.Hat, Beanie, Scarf, Gloves, Belt, Sunglasses, Watch, Tie, Jewelry, Shuka.Bags & LuggageFunctional storage items carried on the person.Tote, Crossbody, Backpack, Clutch, Satchel, Duffle, Suitcase.Underwear & Base LayersIntimate apparel and thermal layers worn directly against the skin.Bra, Underwear, Shapewear, Thermal Top, Thermal Bottom, Socks.Sleepwear & LoungewearGarments strictly designed for home relaxation or sleeping.Pajamas, Nightgown, Robe, Lounge Pants.Sets & Co-ordsPre-matched, multi-piece outfits designed to be worn simultaneously.Suit (2-piece/3-piece), Tracksuit, Matching Set, Mushanana.Functional and Contextual TaggingBy decoupling functional context from the structural hierarchy, the database can easily accommodate a "Trench Coat" that is simultaneously tagged for "Rainy" weather and "Work" occasions. These function-based categories should be implemented as indexed many-to-many relationships in the database schema.Tag CategoryApplication & LogicExample TagsFunction / OccasionDictates the social or functional appropriateness of the garment.Casual, Formal, Athletic, Work/Office, Travel, Ceremonial, Wedding, Homewear, Uniform, Date Night.Weather / ClimateDefines the environmental conditions the garment can withstand.Hot, Cold, Rainy, Dry, Humid, Transitional, Indoor, Outdoor.SeasonalityMaps to traditional retail and climatic seasons, useful for closet rotation.Summer, Winter, Spring, Fall, Rainy Season, Dry Season.This separation is highly practical for global climate variations. For instance, planning a wardrobe for Kampala requires understanding that the "Rainy Season" (March–May) involves high humidity and sudden downpours, dictating a need for breathable, quick-drying fabrics tagged with "Humid" and "Rainy". A rigid taxonomy would struggle to classify a lightweight, waterproof windbreaker, but a tagged system easily identifies it as Outerwear > Jacket with tags [Rainy, Humid, Travel, Casual].Attribute Model and Field Schema DesignDetermining which attributes should be top-level (represented as distinct columns in a SQL database) versus nested (stored as JSON payloads within a single column) is a critical architectural decision. Top-level attributes should be strictly limited to fields required for primary filtering, sorting, and relational joins. Nested attributes should encompass high-granularity metadata—such as complex construction details or granular material properties—that are typically displayed on an item's detail page but rarely queried across the entire database simultaneously.The schema is divided into six logical domains to ensure flexibility for future barcode, OCR, and avatar mapping features, while remaining efficient for local mobile computation.Identity and Visual FieldsIdentity fields establish the absolute uniqueness and core classification of the item, while visual fields drive the user interface, search algorithms, and the visual outfit generation engine.Field NameData TypeImplementation TierTechnical Logic & Business Purposeitem_idUUIDv1 MinimalPrimary key ensuring unique identification across local and synced environments.user_idUUIDv1 MinimalForeign key supporting multi-tenant cloud sync or multi-profile local usage.nameStringv1 MinimalUser-defined title (e.g., "Navy Zara Trench") for text-based searching.primary_categoryEnumv1 MinimalMaps to the Level 1 structural hierarchy (e.g., OUTERWEAR).sub_categoryEnumv1 MinimalMaps to the Level 2 shape descriptor (e.g., TRENCH_COAT).image_pathStringv1 MinimalLocal file path to the user's uploaded image. Crucially, this image should undergo on-device background removal via models like CoreML or ML Kit.primary_color_idIntegerv1 MinimalForeign key mapping to the unified color table for top-level color filtering.secondary_colorsJSON Arrayv2 ExpandedArray of integer foreign keys supporting items with dual or multiple distinct colors.patternEnumv1 MinimalSolid, Striped, Plaid, Checked, Floral, Geometric, Polka Dot, Camo, Abstract, Graphic, Animal Print.surface_finishEnumv2 ExpandedMatte, Glossy, Woven, Knitted, Distressed, Washed, Faded, Metallic. Essential for texturing in future 3D avatar rendering.Material, Care, and Construction FieldsThese fields are essential for automating laundry tracking, promoting garment longevity, and calculating sustainability metrics. Construction details greatly influence both visual styling and 3D mesh deformation in advanced avatar applications.Field NameData TypeImplementation TierTechnical Logic & Business Purposedominant_materialEnumv1 MinimalCotton, Denim, Wool, Linen, Polyester, Rayon, Silk, Nylon, Spandex, Leather, Suede. Used to infer default laundry instructions if none are explicitly provided.material_compositionString/JSONv2 ExpandedDetailed breakdown (e.g., "90% Cotton, 10% Elastane"). Ideal target for automated OCR extraction from photographed care tags.material_propertiesJSON Arrayv2 ExpandedBreathable, Stretch, Heavyweight, Insulated, Wrinkle-Resistant, Water-Resistant. Informs AI outfit recommendations based on local weather APIs.collar_typeEnumv2 ExpandedCollared, Mandarin, Spread, Button-Down, Polo, Hooded, Round/Crew, V-Neck, Lapel.fastening_typeEnumv2 ExpandedButtoned, Zippered, Pullover, Lace-up, Snap, Hook-and-Eye, Elastic.embellishmentsJSON Arrayv2 ExpandedEmbroidery, Print, Applique, Beading, Sequins, Patchwork. Highly relevant for predicting delicate care requirements.stitching_detailsJSON Arrayv2 ExpandedTopstitching, Overlocked, Flatlock, Reinforced Seams, Blind Hem, Raw Hem, Quilted.care_instructionsJSON Objectv2 ExpandedA structured payload housing ISO 3758 laundry symbols and their boolean/integer states, discussed extensively in the subsequent section.Brand, Authenticity, and Usage HistoryDistinguishing between brand, manufacturer, and origin is critical. The "Brand" is the consumer-facing label (e.g., Nike), while the "Manufacturer" is the corporate parent or specific factory entity, which becomes vital when dealing with luxury conglomerates or ethical sourcing transparency. Usage history transforms the application from a static visual catalog into an active management and analytics engine.Field NameData TypeImplementation TierTechnical Logic & Business Purposebrand_nameStringv1 MinimalThe primary label associated with the garment.manufacturerStringv2 ExpandedThe parent company or producing entity, relevant for sustainability tracking.country_of_originStringv2 ExpandedCaptured via OCR on neck tags (e.g., "Made in Italy").authenticityEnumv2 ExpandedOriginal, Replica, Handmade, Thrifted, Gifted, Unknown. Highly relevant as secondhand markets grow.gtin_upcStringv2 ExpandedUniversal Product Code or Global Trade Item Number. The gateway for automated metadata fetching via GS1 Digital Links.statusEnumv1 MinimalClean, Dirty, In Laundry, At Dry Cleaner, Needs Repair, Retired. The outfit engine must aggressively filter out non-clean items.wear_countIntegerv1 MinimalIterates upwards every time the item is logged in a daily outfit.purchase_priceDecimalv2 ExpandedEnables the calculation of virtual fields.cost_per_wearVirtual/Calcv2 ExpandedDerived dynamically as purchase_price / wear_count. A primary metric for sustainable wardrobe management.last_worn_dateTimestampv1 MinimalAllows users to filter for neglected items or track rotation freshness.item_completenessEnumv2 ExpandedSingle Item, Paired Item (gloves, shoes), Set Part. Ensures the outfit engine does not recommend a single left shoe.compatibility_tagsJSON Arrayv2 ExpandedSpecific UUIDs of other items this garment pairs well with, allowing for manual hardcoding of preferred combinations.Fit, Cut, and Silhouette FieldsThese fields are the most critical for future-proofing the application for avatar mapping. When attempting to overlay 2D garment imagery onto 3D rigs, the software must understand how the fabric is intended to drape and interact with underlying layers.Field NameData TypeImplementation TierTechnical Logic & Business Purposesize_labelStringv1 MinimalAlphanumeric size (e.g., "M", "32x34", "UK 10").fit_typeEnumv2 ExpandedRegular, Slim, Loose, Oversized, Skinny, Fitted. Dictates bounding box dimensions in visual rendering.cut_silhouetteEnumv2 ExpandedTapered, Straight, Bootcut, Wide-Leg, Cropped, A-Line, Asymmetrical.layering_indexIntegerv2 ExpandedCritical for 3D topologies. Establishes a strict Z-index for the garment. 0 = Skin/Base, 1 = Mid-layer, 2 = Outerwear, 3 = Heavy Overcoat. Prevents logical errors like rendering a jacket beneath a t-shirt.Fabric, Material, and Construction AnalyticsThe integration of granular fabric and construction details is not merely an exercise in data collection; it actively drives the intelligence of the application's recommendation and care engines.The material_properties array bridges the gap between the physical reality of a garment and the meteorological data pulled from weather APIs. For example, linen and lightweight cotton possess high "breathability" and low "insulation." If the user is traveling to Kampala during the hot, humid drier season (December–February, temperatures reaching 30°C), the recommendation engine scans the database for garments tagged with these specific material properties, effectively automating the packing process. Conversely, materials with high "insulation" and "water resistance" (such as treated wool or Gore-Tex) are surfaced when the weather API reports dropping temperatures and precipitation.Construction details, such as stitching_details and embellishments, serve a dual purpose. Visually, a garment with a "raw hem" and "distressing" requires a different aesthetic pairing than a garment with "blind hems" and "topstitching." More importantly, these fields feed directly into the automated care logic. An application evaluating a silk blouse with "beading" and "sequins" must immediately override any standard washing defaults and flag the item as requiring highly delicate, professional care to prevent irreversible damage during agitation.The ISO 3758 Laundry and Care TaxonomyGlobal textile care is strictly standardized under ISO 3758, managed internationally by GINETEX. A digital wardrobe app must internalize this regulatory framework into its database while translating the esoteric symbols into immediate, frictionless, actionable UI elements for the end-user. Expecting consumers to memorize the exact differences between single-bar and double-bar agitation is a UX failure; the app must perform the translation automatically.Database Representation of Care InstructionsThe optimal architecture stores care instructions within a top-level care_instructions JSON object. This allows for vast flexibility without bloating the SQLite database with dozens of highly sparse columns. The JSON object should be segmented into the five primary ISO 3758 operations: Washing, Bleaching, Drying, Ironing, and Professional Cleaning.JSON{
  "wash": {
    "max_temp_c": 30,
    "agitation_level": 1, 
    "hand_wash_only": false,
    "do_not_wash": false
  },
  "bleach": {
    "chlorine_allowed": false,
    "oxygen_allowed": true,
    "do_not_bleach": false
  },
  "dry": {
    "method": "tumble",
    "temp_level": 1,
    "do_not_dry": false
  },
  "iron": {
    "max_temp_c": 110,
    "steam_allowed": true,
    "do_not_iron": false
  },
  "professional": {
    "solvent_type": "PCE",
    "wet_clean": false,
    "do_not_clean": false
  }
}
Translating ISO Syntax to User InterfaceIn the application UI, the visual ISO symbol should be displayed alongside a plain-text, human-readable string generated dynamically from the JSON payload.Care CategoryISO Visual SyntaxUnderlying Database LogicDynamic App UI TranslationWashing (Tub)Number inside tubwash.max_temp_c"Machine wash at max [X]°C"Hand inside tubwash.hand_wash_only: true"Gentle hand wash only"Single bar under tubwash.agitation_level: 1"Delicate cycle / Mild spin"Double bar under tubwash.agitation_level: 2"Very delicate / Minimum spin"Crossed-out tubwash.do_not_wash: true"Do not wash at home"Bleaching (Triangle)Empty trianglebleach.chlorine_allowed: true"Any bleach allowed"Two diagonal linesbleach.oxygen_allowed: true"Non-chlorine (oxygen) bleach only"Crossed-out trianglebleach.do_not_bleach: true"Do not bleach"Drying (Square)Circle inside (Tumble)dry.method: "tumble""Tumble dry allowed"Dots inside circledry.temp_level: 1, 2, or 3"Tumble dry: Low / Med / High heat"Vertical line in squaredry.method: "line_dry""Hang to dry"Horizontal linedry.method: "flat_dry""Dry flat (reshape while damp)"Ironing (Iron)Dots inside ironiron.max_temp_c: 110, 150, 200"Iron: Low / Med / High heat"Crossed-out ironiron.do_not_iron: true"Do not iron"Prof. Clean (Circle)Letter P or F in circleprofessional.solvent_type: "PCE""Dry clean only (Standard solvent)"Letter W in circleprofessional.wet_clean: true"Professional wet clean required"Inferred Defaults via Heuristics: To minimize data entry fatigue, if the care_instructions JSON is empty, the application should deploy a heuristic engine based on the dominant_material field. For instance, if the material is identified as "Wool" or "Cashmere," the engine should automatically populate a draft JSON payload indicating hand_wash_only: true, flat_dry: true, and do_not_tumble_dry: true, presenting this to the user for quick confirmation.Color Architecture and Semantic MappingColor perception in human cognition is highly nuanced, whereas computer vision systems process color rigidly as RGB or Hexadecimal codes. In a wardrobe application, displaying the hex code #3EB489 as "Mint Green" is essential for a polished user experience. However, the filtering system requires much broader categorization; if a user filters their wardrobe by "Green," #3EB489 must be included in the results.The Two-Tier Color ArchitectureTo bridge the gap between human perception and machine precision, color must be mapped on two synchronized levels:Semantic Name (Specific): The highly descriptive name applied to the specific hex code (e.g., "Navy", "Burgundy", "Teal").Color Family (Broad): The primary category used for macro-filtering (e.g., "Blue", "Red", "Green").The application should standardize around 20 user-facing Color Families: Black, White, Gray, Navy, Blue, Light Blue, Red, Burgundy, Pink, Green, Olive, Yellow, Beige, Tan, Brown, Orange, Purple, Gold, Silver, and Multicolor/Patterned.Mathematical Mapping: Hex to Semantic NameThe RGB color space is notoriously inadequate for calculating perceptual similarities. The Euclidean distance between two RGB hex values does not accurately correlate with how the human eye perceives distance between colors. To achieve accurate mapping, the application must convert RGB values into the CIE Lab (L*a*b*) color space.In the Lab color space, L represents lightness, a represents the green-to-red axis, and b represents the blue-to-yellow axis. The mathematical distance between two colors in this space (known as Delta-E or $\Delta E_{76}$) closely mirrors human visual perception.When a user photographs a garment, the app executes the following local pipeline:Extraction: Utilize a K-Means clustering algorithm on the segmented, background-removed garment image to extract the dominant hex code.Conversion: Translate the extracted hex string into the L*a*b* coordinate format.Nearest Neighbor Search: Calculate the $\Delta E$ distance against a pre-loaded local SQLite dictionary of named colors. The entry yielding the lowest $\Delta E$ is selected and assigned to the garment.Recommended Open-Source LibrariesFor offline-first mobile applications, shipping a massive color dictionary creates unnecessary application bloat.ntc.js (Name That Color): A highly optimized, lightweight library containing approximately 1,500 named colors. It is exceptionally well-suited for mobile integration, performing L*a*b* conversions and lookups locally in milliseconds.color-name-list: A massive open-source dataset containing over 31,000 color names.Implementation Strategy: Do not embed the entire 31,000-item list. Instead, utilize the curated "Short Names subset" or "Best of Names subset". This avoids the assignment of overly abstract or metaphorical color names (e.g., "Odysseus Blue" or "Tropical Vacation"), which degrade the precision of the UI and confuse users. Every specific color in the embedded SQLite dictionary must be pre-mapped via a hard-coded relationship to one of the 20 primary Color Families.Handling Patterns and Multicolor GarmentsFor garments with heavy patterns (e.g., Plaid, Floral, Camouflage), the K-Means extraction algorithm will return multiple significant clusters.If one cluster dominates >85% of the surface area, the garment is assigned a single primary_color_id and flagged as Solid.If two clusters are relatively evenly split (e.g., a striped shirt), the system populates both primary_color_id and secondary_colors.If three or more clusters hold significant statistical weight, the system tags the item's color family as Multicolor and relies entirely on the pattern descriptor (e.g., Floral, Geometric) for visual classification.Filter Engineering for Wardrobe ManagementWardrobe management software fundamentally transcends standard retail filtering by introducing temporal, behavioral, and status-based dimensions. A highly effective filtering UI utilizes progressive disclosure: exposing common, immediate-need filters at the top of the interface while hiding highly granular technical details behind an "Advanced Filters" modal.Quick Filters (Immediate Access)Status: Clean, In Laundry, Dirty, At Dry Cleaner. This is the most critical filter for daily use; the outfit generation engine must possess a hard constraint to never recommend a dirty or out-for-repair garment.Weather Tolerance: Hot, Cold, Rain/Wet.Category Family: Rapid toggles for Tops, Bottoms, Footwear, Outerwear.Advanced Filters (Detailed Sorting)Color Family: A visual grid of swatches representing the 20 primary color families.Occasion / Function: Casual, Work, Formal, Gym, Night Out.Fit & Silhouette: Loose, Regular, Slim, Cropped.Brand & Authenticity: A dynamic, searchable list generated from the user's populated database.Analytics & Usage Filters (Wardrobe Health)Wear Frequency: Allows users to filter for "Rarely Worn" (e.g., worn less than twice in the last six months) to identify items for donation, or "Frequently Worn" to identify staple pieces.Cost Per Wear (CPW): A slider mechanism allowing users to find items with a CPW greater than $10 (indicating poor return on investment) or less than $1 (indicating high value).Last Worn: A temporal filter (e.g., "Show items I haven't worn since last Winter").Extending into the Future: Barcodes, OCR, and 3D AvatarsTo ensure the offline-first architecture scales gracefully over years of updates, the schema must preemptively accommodate hardware-accelerated machine learning and standardized global commerce integrations. The data foundation laid in the schema directly dictates the feasibility of these advanced features.Barcode, UPC, and GS1 Digital Link IntegrationThe retail industry is actively transitioning from standard 1D barcodes to 2D GS1 Digital Links—QR codes that carry Global Trade Item Number (GTIN) data alongside standard URLs. By explicitly including a gtin_upc field in the schema, the application is prepared for seamless barcode scanner integrations.The Future Workflow: A user scans a tag on a newly purchased item. The application reads the UPC and queries an external product database (such as Go-UPC or BarcodeLookup) via a lightweight API call. The payload returns the Brand, Material Composition, Color, and Item Name, automating the data entry process entirely.Offline Fallback Mechanism: If the user scans an item while the device is offline, the UPC string is stored locally in a queue. Once a network connection is restored, a background worker dispatches the API queries and silently backfills the rich metadata into the item records.On-Device Machine Learning: Background Removal and OCRBackground Removal: Isolating the garment from the background is essential for generating clean, visually appealing digital lookbooks. Utilizing on-device models, such as Apple's CoreML (Vision framework) or Google's ML Kit, image segmentation algorithms can classify pixels belonging to the garment versus the background flawlessly, entirely offline.Optical Character Recognition (OCR): When a user photographs a garment's physical care label, on-device OCR can parse the text (e.g., "100% Cotton", "Made in Italy") and automatically populate the material_composition and country_of_origin fields. Furthermore, fine-tuned vision models can be trained specifically on the ISO 3758 dataset to detect and decode care symbols directly from the photograph, eliminating the need for the user to manually select the washing parameters.3D Avatar Mapping and Topological ConsiderationsThe bleeding edge of fashion technology involves Virtual Try-On (VTON) environments and real-time 3D avatars. If the application roadmap includes mapping 2D clothing assets to a 3D model (e.g., exporting GLTF files or creating layered clothing for platforms like Unity or Roblox), it requires highly specific physics and topological metadata.The Critical Nature of the Layering Index: In 3D graphics, layered clothing requires an "Inner Cage" (the 3D surface the garment wraps around) and an "Outer Cage" (the surface the subsequent layer of clothing wraps around) to prevent clipping—a graphical error where textures bleed through one another. The layering_index field (values 0-3) is the database representation of this physical reality. A base layer (a t-shirt, index 0) provides the inner cage for a mid-layer (a sweater, index 1). The database must strictly enforce these layering hierarchies to ensure the outfit builder engine does not mathematically attempt to place a heavy coat underneath a tight undershirt.Silhouette Data: The fit_type and cut_silhouette fields (e.g., Slim vs. Oversized) dictate the specific mesh deformation algorithms required when stretching a flat 2D texture over a volumetric 3D avatar rig. Capturing this data early ensures historical wardrobe items are ready for 3D rendering when the feature launches.Implementation Strategy: v1 Minimal vs. v2 Expanded SchemasAttempting to launch the full, comprehensive schema immediately will overwhelm development constraints and result in severe user abandonment due to excessive manual data entry requirements. A phased, intelligent rollout is mandatory to balance utility with UX friction.The Minimal v1 SchemaCore Objective: Achieve near-zero friction during manual item entry, ensure offline-first reliability, establish core outfit generation logic, and provide basic usage tracking.Required Implementation:item_id and image_path (Stored locally; background removal executed seamlessly via on-device native APIs).primary_category and sub_category (Mandatory for UI grouping and structural logic).primary_color_id (Mapped to the 20 basic families, automatically extracted via K-Means clustering upon image capture).status (Clean / Dirty / In Laundry).wear_count and last_worn_date.The v1 User Experience: The user photographs a shirt. In the background, the app automatically removes the background, detects the dominant color, and suggests the category. The user verifies and hits "Save." The entire ingestion process introduces a maximum of three seconds of friction, establishing immediate value.The Expanded v2 SchemaCore Objective: Unlock advanced sustainability analytics, nuanced care tracking, AI-driven outfit recommendations, and fully automated metadata ingestion via hardware integrations.Introduced Fields & Features:care_instructions (The ISO 3758 JSON structure), auto-filled via on-device OCR scanning of the neck tag.gtin_upc, populated by scanning product barcodes to trigger automated metadata retrieval from global databases.purchase_price, cost_per_wear, and granular usage metrics.layering_index, preparing the system for advanced 2D visual layering and serving as the pre-computed topology map for future 3D avatar rendering pipelines.Granular material_composition, stitching_details, and fit_type.The v2 User Experience: The user adds items rapidly using the v1 workflow. Later, during passive downtime, they can open an existing item's detail page, tap a "Scan Label" button, and utilize the camera to ingest the complex metadata (materials, origin, care symbols) via CoreML/TensorFlow Lite models, bypassing the need for tedious manual data entry entirely. This two-tiered approach ensures maximum adoption while steadily building a dataset rich enough to power sophisticated, next-generation wardrobe intelligence.


Here’s a researched, opinionated answer to your prompt: a **taxonomy, item schema, filter set, color naming strategy, and laundry model**, all tuned for an offline-first personal wardrobe app rather than ecommerce.

I’ll structure it along the outputs you asked for.

***

## 1. Recommended clothing taxonomy (category hierarchy)

This is a pragmatic, user-facing taxonomy, loosely aligned with fashion tagging systems used in industry, but simplified for personal use. [docs.ximilar](https://docs.ximilar.com/taxonomy/fashion)

### 1.1 Top-level categories

- **Tops**
- **Bottoms**
- **One-piece garments**
- **Outerwear**
- **Footwear**
- **Accessories**
- **Underwear & lingerie**
- **Sleepwear & loungewear**
- **Activewear & sportswear**
- **Formalwear**
- **Workwear & uniforms**
- **Traditional / cultural wear**

### 1.2 Category → type mapping

Examples (not exhaustive, but a solid base):

**Tops**
- T-shirt
- Tank top
- Long-sleeve top
- Blouse
- Shirt (dress shirt, casual shirt)
- Polo
- Henley
- Hoodie
- Sweatshirt
- Crop top
- Camisole

**Bottoms**
- Jeans (straight, skinny, bootcut, wide-leg)
- Trousers / chinos
- Joggers / sweatpants
- Cargo pants
- Shorts
- Skirt (mini, midi, maxi)

**One-piece garments**
- Dress (casual, formal)
- Jumpsuit
- Playsuit / romper
- Overall / dungaree
- Tunic (if treated as dress-length)

**Outerwear**
- Jacket (denim, bomber, leather, puffer)
- Coat (trench, overcoat, parka)
- Blazer
- Cardigan
- Gilet / vest

**Footwear**
- Sneakers
- Loafers
- Boots (ankle, mid, knee-high)
- Sandals
- Slides / flip-flops
- Heels (pumps, stilettos, block heels)
- Flats
- Formal dress shoes

**Accessories**
- Bags (backpack, tote, crossbody, clutch)
- Belts
- Scarves
- Hats (cap, beanie, sun hat)
- Gloves
- Jewelry (necklace, ring, bracelet, earrings)
- Sunglasses

**Underwear & lingerie**
- Briefs / boxers
- Bra (sports, underwire, bralette)
- Undershirt
- Shapewear

**Sleepwear & loungewear**
- Pajama top / bottom
- Nightdress
- Robe
- Lounge set
- Slippers

**Activewear & sportswear**
- Sports bra
- Leggings / tights
- Training shorts
- Track jacket
- Jerseys
- Compression wear

**Formalwear**
- Suit jacket
- Suit trousers
- Waistcoat
- Evening dress / gown
- Dress shirt

**Workwear & uniforms**
- Scrubs
- Work coveralls
- Safety boots
- Business shirt and trousers (can overlap with formalwear but flagged via occasion/usage tags)

**Traditional / cultural wear**
- Kitenge / gomesi / kanzu (East African)
- Kurta / sherwani / sari / kimono / etc.
- Headscarves / cultural hats

You don’t need to ship the full global cultural set in v1; you can treat “Traditional wear” as a category with freeform subtype plus tags, then expand later.

### 1.3 Function / occasion tags

Not a category, but a **dimension** applied via tags:

- Function / occasion:
  - Casual
  - Formal
  - Workwear / office
  - Streetwear
  - Athletic / gym
  - Travel
  - Homewear
  - Ceremonial / wedding
  - Religious / church / mosque / temple
  - Party / nightlife
- Season:
  - Summer
  - Winter
  - Rainy season
  - Transitional
- Weather:
  - Hot / cold / humid / dry / rainy / windy
- Indoor / outdoor

These are tags, not categories, so one item can be “Casual + Winter + Outdoor”.

***

## 2. Field schema for wardrobe items

Split into identity, visual, material/care, brand/authenticity, usage/history, and fit/construction.

### 2.1 Identity fields

- `itemId` (internal ID)
- `name` (user-defined)
- `category` (top-level; e.g., Tops, Bottoms)
- `type` (subtype; e.g., T-shirt, Sneakers)
- `gender/use` (optional; e.g., Men, Women, Unisex, Kids, or omit)
- `ownedByProfile` (for multi-profile later; nullable in v1)

### 2.2 Visual fields

- `primaryColor` (hex + semantic name)
- `secondaryColor` (optional)
- `pattern` (Solid, Striped, Plaid, Checked, Floral, Geometric, Polka dot, Camo, Abstract, Graphic, Textured, Ribbed, Other)
- `finish` (Matte, Glossy, Woven, Knitted, Distressed, Washed, Faded, Embroidered, Printed)
- `images`: list of photos (each with URI, thumbnail, isPrimary, takenAt)

### 2.3 Material & care fields

**Material**

- `fabricPrimary` (Cotton, Denim, Wool, Linen, Polyester, Rayon, Viscose, Silk, Satin, Nylon, Spandex/Elastane, Acrylic, Leather, Suede, Canvas, Knit, Fleece, Terry, Cashmere, Other)
- `fabricSecondary` (optional blend)
- `materialProperties` flags:
  - Breathable
  - Stretch
  - Lightweight / Heavyweight
  - Insulating
  - Wrinkle-resistant
  - Water-resistant
  - Delicate

**Care**

- `washMethod` (Machine wash, Hand wash, Do not wash)
- `washTemp` (Cold, 30°C, 40°C, 60°C, Other)
- `bleachRule` (Bleach allowed, Non-chlorine only, No bleach)
- `dryMethod` (Tumble dry, Line dry, Flat dry, Drip dry, Shade dry, Do not tumble dry)
- `tumbleDryTemp` (Low, Medium, High, No heat)
- `ironRule` (Iron allowed, Low/Medium/High temp, Do not iron, Steam allowed)
- `dryCleanRule` (Dry clean allowed, Dry clean only, No dry clean)
- `careSymbols` (normalized set of laundry icons, mapped from care label) [youtube](https://www.youtube.com/watch?v=agN4BsKxnTk)

In v1, you can store **simplified fields** (washMethod, washTemp, dryMethod, ironRule), and optionally store full symbol data as a structured payload parsed via care-tag OCR later. [sartorbohemia](https://www.sartorbohemia.com/article/26/laundry-symbols-guide/)

### 2.4 Brand & authenticity fields

- `brandName`
- `subBrand` (e.g., Nike Sportswear)
- `labelName` (if different)
- `manufacturer` (optional, often same as brand)
- `countryOfOrigin` (ISO country code; from care tag where available) [gov](https://www.gov.uk/guidance/classifying-textile-apparel)
- `placeOfManufacture` (free text, optional)
- `authenticity` (Original, Replica, Gifted, Handmade, Thrifted, Unknown)
- `sku` / `barcode` (optional, for future API)
- `purchasePrice`
- `purchaseDate`
- `purchaseLocation` (store or URL)

### 2.5 Usage & history fields

- `timesWorn` (denormalized count)
- `lastWornAt`
- `wearEvents` (separate table; each event has itemId/outfitId, dateTime, context tags, notes)
- `costPerWear` (derived: purchasePrice / timesWorn)
- `cleanliness` (Clean, Worn, Dirty, InLaundry)
- `condition` (New, Good, Worn, NeedsRepair, Torn, Retired)
- `ownershipStatus` (Active, Retired, Deleted)
- `addedAt`

### 2.6 Fit, cut, silhouette & construction

**Fit & silhouette**

- `fit` (Loose, Regular, Slim, Oversized, Relaxed, Skinny, Cropped, Fitted)
- `cut` (Straight, Tapered, Bootcut, Wide-leg, A-line, Bodycon, Boxy, Other)
- `length` (Cropped, Hip, Thigh, Knee, Midi, Maxi)

**Collar & neckline**

- `collarType` (Collared, Mandarin, Spread, Button-down, Polo, Hooded, None, Other)
- `neckline` (Round, V-neck, Scoop, Off-shoulder, Square, High neck, Crew, Other)

**Fastening**

- `fasteningType` (Buttoned, Zippered, Pullover, Lace-up, Snap, Hook-and-eye, Elastic, Buckle, Slip-on)

**Embellishment & stitching**

- `embellishment` (Embroidery, Print, Applique, Beading, Sequins, Patchwork, Distressing, None)
- `stitchingDetails` (Topstitching, Double-stitched, Overlocked, Flatlock, Reinforced seams, Blind hem, Raw hem, Quilted) – realistically, this is an advanced detail, so v1 could reduce this to a simple “Reinforced” flag.

### 2.7 Occasion, season, weather

- `occasionTags` (Work, Casual, Formal, Wedding, Travel, Gym, Date, Church, Party, Home, Other)
- `seasonTags` (Summer, Winter, Rainy season, Transitional)
- `weatherTags` (Hot, Cold, Rainy, Humid, Dry, Windy)
- `indoorOutdoor` (Indoor, Outdoor, Both)

***

## 3. Recommended filter set for the app UI

Design filters to cover the most common queries:

**Core filters (always available)**

- Category (Tops, Bottoms, Footwear, etc.)
- Type (T-shirt, Sneakers, Dress...)
- Cleanliness (Clean, Dirty, InLaundry)
- Condition (Good, NeedsRepair, Torn, Retired)
- Color (semantic names, swatches)
- Brand
- Season (Summer, Winter, Rainy, Transitional)
- Occasion (Work, Casual, Formal, Gym, Date, Party...)

**Secondary filters (in a “More filters” sheet)**

- Material (cotton, denim, leather etc.)
- Pattern (solid, striped, plaid...)
- Fit (slim, oversized...)
- Length (cropped, midi, maxi)
- Fastening (buttoned, zippered, pullover)
- Collar/neckline (collared, hooded, v-neck)
- Weather tags (hot, cold, rainy)
- Authenticity (original, thrifted...)
- Place of manufacture (region)
- Ownership (active/retired)

**History & analytics filters**

- Last worn (e.g., “Not worn in 60+ days”)
- Wear count (frequently worn vs rarely worn)
- Cost per wear (high vs low)
- Items needing repair (condition = NeedsRepair/Torn)
- Items in laundry

**Outfit-specific filters**

- Outfit completeness (single piece, paired item, set, full outfit)
- Avatar compatibility (if later you need e.g., “can be mapped to avatar slot X”)

***

## 4. Color naming strategy & libraries

You want:

- Hex storage (for dynamic theming and UI).
- Semantic names for filtering and readability.
- Not a crazy 500‑name palette; something users recognize and can search.

**Strategy:**

1. Store colors as **hex** plus a **semantic name**.
2. Use a fixed **base palette** for user-facing names:
   - black, white, gray, navy, blue, light blue, teal, red, burgundy, pink, green, olive, yellow, beige, tan, brown, orange, purple, gold, silver, multicolor, patterned.
3. Map hex → nearest semantic name via a small mapping function or library. [rgbatohex](https://rgbatohex.com/tools/color-name)
   - You can use ideas from CSS color name lists (140 standard names) or an existing hex-to-name library, but map them down to your reduced set for filtering. [github](https://github.com/jeff3754/HexColorToColorName)

**Implementation options:**

- Use a simplified copy of a hex-to-name library (e.g., HexColorToColorName) and then map detailed names (e.g., “DodgerBlue”) to your coarse categories (blue). [rgbatohex](https://rgbatohex.com/tools/color-name)
- Keep a **manual override**: user can pick a semantic name from your list even if the automatic mapping is slightly off.

Patterns/prints get `patterned` or `multicolor` flag; primaryColor can be the “dominant” color while pattern semantics are stored separately.

***

## 5. Laundry care symbol model (for UI)

Laundry symbols are standardized (wash tub, triangle, square, iron, circle for dry-clean). [blog.treasurie](https://blog.treasurie.com/laundry-symbols/)

For UI you want a **simple, normalized model**:

**Master list of symbol groups:**

- Wash (tub, temp, gentle lines, hand wash)
- Bleach (triangle: allowed, non‑chlorine only, forbidden)
- Dry (square symbols: tumble dry with dot temperature; line dry; flat dry; drip dry; shade dry)
- Iron (iron with dot temperature; no steam; no iron)
- Dry clean (circle with letters/lines; simplified as “dry-clean only / allowed / no dry clean”)

**Data model for care:**

- `washSymbol` (enum with values like MachineCold, MachineWarm, HandWashOnly, DoNotWash)
- `bleachSymbol` (BleachAllowed, NonChlorineOnly, DoNotBleach)
- `drySymbol` (TumbleLow, TumbleMedium, TumbleHigh, NoTumble, LineDry, FlatDry, DripDry, ShadeDry)
- `ironSymbol` (IronLow, IronMedium, IronHigh, DoNotIron, SteamAllowed)
- `dryCleanSymbol` (DryCleanAllowed, DryCleanOnly, WetCleanOnly, DoNotDryClean)

**UI representation:**

- Use icons consistent with international standards:
  - Tub, triangle, square, iron, circle. [en.wikipedia](https://en.wikipedia.org/wiki/Laundry_symbol)
- Show a **short text label** next to icons in detail screens; don’t rely on icons alone.
- In v1, let users pick from a simplified set (e.g., “Machine wash cold”, “Line dry”, “Do not bleach”); in v2, import care symbols from OCR and map them into this model.

***

## 6. Minimal v1 schema vs expanded v2 schema

### 6.1 Minimal v1 schema (offline-first, manual entry)

Focus on fields that are easy to fill and highly useful:

**Identity:**
- itemId
- name
- category
- type

**Visual:**
- primaryColor (hex + coarse semantic)
- pattern (simple enum)
- image (one primary photo; more optional)

**Material & care:**
- fabricPrimary (coarse list)
- washMethod (Machine/Hand/None)
- washTemp (Cold/Warm/Hot)
- dryMethod (Tumble/Line/Flat)
- ironRule (Yes/No)
- basic care flags (Delicate, Dry-clean only)

**Brand/authenticity:**
- brandName
- countryOfOrigin (optional)
- purchasePrice (optional)
- purchaseDate (optional)
- authenticity (Original/Thrifted/etc., optional)

**Usage/history:**
- cleanliness (Clean/Dirty/InLaundry)
- condition (Good/NeedsRepair/Torn/Retired)
- timesWorn
- lastWornAt

**Fit/construction:**
- fit (Loose/Regular/Slim/Oversized/etc.)
- length (Cropped/Hip/Knee/Midi/Maxi)
- collarType (basic options)
- fasteningType (buttoned/zippered/pullover/etc.)

**Occasion/season:**
- occasionTags (small set)
- seasonTags (summer/winter/rainy/transitional)
- weatherTags (optional)

This v1 schema should be **quick to fill** using forms and chips.

### 6.2 Expanded v2 schema (for advanced users, metadata import, avatar)

Add:

- Detailed fabricSecondary and material properties (stretch, water-resistant, etc.).
- Full careSymbols, imported via care-tag OCR.
- SubBrand, manufacturer, placeOfManufacture, SKU/barcode.
- More detailed stitching and embellishment fields.
- Avatar slot mapping (e.g., torsoUpper, torsoLower, feet).
- Additional cultural clothing types with localized names.
- Size system (US/UK/EU), body measurements, silhouette descriptors for more accurate avatar fit.
- More intricate analytics fields (computed, not manually entered).

All v2 fields should be **optional** and surfaced in advanced edit screens, not forced on casual users.

***

## 7. Missing taxonomy categories worth adding

From industry fashion tagging / ecommerce taxonomies, a few extra useful categories: [hypotenuse](https://www.hypotenuse.ai/features/automatic-fashion-product-tagging)

- **Swimwear** – bikinis, one‑pieces, trunks, rashguards, boardshorts.
- **Jewelry** – separated under accessories (bracelets, rings, earrings, necklaces).
- **Headwear** – caps, beanies, sun hats, headwraps.
- **Kids’ clothing** – onesies, rompers, pinafores, school uniforms.
- **Performance tags** – stretch, breathable, quick-dry, UV protection, water‑repellent (these can be materialProperties flags). [hypotenuse](https://www.hypotenuse.ai/features/automatic-fashion-product-tagging)

You don’t have to hardwire everything in v1, but it’s worth designing the schema so these can be added as types or tags without breaking the model.

***

### How to use this

Given your architecture, you can now:

- Turn the taxonomy into **enum / sealed class definitions** (`Category`, `Type`, `Material`, `Pattern`, `Fit` etc.).
- Implement the v1 schema in Room.
- Use the filter set as the basis for **Closet filter UI**.
- Start with a **simple color naming mapping**, then later plug in a small hex-to-name library if needed. [github](https://github.com/jeff3754/HexColorToColorName)
- Implement a **basic care model** initially and plan a v2 where care-tag OCR can feed structured careSymbols.

If you want, next step I can do is generate a **Kotlin data model sketch** (entities + enums) directly from this schema, ready for your `core:model` and `core:database` modules.



ANOTHER RESEARCH

Taxonomy and Attribute Architecture for a Practical Digital Wardrobe Ecosystem
The conceptualization of a digital wardrobe management system requires a fundamental paradigm shift away from traditional retail and e-commerce taxonomies. E-commerce classification architectures are fundamentally designed to facilitate product discovery and financial transactions, relying heavily on granular, trend-driven, and highly nested categories that prioritize search engine optimization over daily utility. Conversely, a personal digital wardrobe operates as a localized inventory and lifestyle management system. Its primary objectives are to facilitate daily outfitting, monitor garment utilization, track lifecycle metrics, and streamline the cognitive load of getting dressed1.
This comprehensive report presents an exhaustive architectural framework for an offline-first, user-centric wardrobe application. It synthesizes wardrobe classification, garment identity, material behavior, laundry semantics, and usage tracking into a structured, scalable data model. The framework is deliberately designed to remain lightweight and accessible for manual data entry in its first iteration, while establishing the rigid, structured schema necessary for future integration with computer vision (CV) auto-tagging, optical character recognition (OCR) for care labels, and three-dimensional avatar mapping3.
The Lived Reality of Wardrobe Management
Understanding user behavior within the context of wardrobe management reveals a stark contrast between how retail catalogs organize clothing and how individuals perceive their personal closets. Consumers categorize their garments based on functional context, physical comfort, and emotional attachment rather than strict structural definitions6.
The modern approach to wardrobe management is heavily influenced by the capsule wardrobe movement and sustainability metrics. Methodologies such as the 5-4-3-2-1 packing rule, the 10x10 challenge, or the Project 333 method dictate that users view their closets in terms of interchangeable modules rather than isolated pieces8. The architecture must support this modularity by allowing items to be grouped into specific stylistic or seasonal "capsules" while maintaining their global identity in the broader wardrobe database.
Furthermore, in a retail database, a garment's state is binary: it is either in stock or out of stock. In a personal wardrobe, a garment exists in a complex, fluctuating lifecycle. It is acquired, worn, placed in an intermediate state (worn but not dirty enough for the laundry), laundered, repaired, seasonally stored, and eventually retired11. An effective system must track these physical states to provide genuine utility, such as preventing an algorithmic outfit generator from recommending a piece that is currently at the dry cleaner or packed away in winter storage11. The primary utility of an outfit planner is reducing decision fatigue. When standing before a closet, individuals evaluate multiple variables simultaneously: ambient weather, destination dress code, physical comfort, and mood. Therefore, the taxonomy cannot be purely structural; it must capture environmental constraints and subjective user preferences14.
Clothing Taxonomy and Wardrobe Classification
A functional taxonomy must strike a delicate balance. It must be broad enough to prevent users from abandoning the manual entry process due to cognitive overload, yet specific enough to support accurate algorithmic outfit generation and eventual 3D avatar collision detection4. The hierarchy must reflect everyday wardrobe use, explicitly incorporating overlooked segments such as loungewear, cultural attire, and modular garments that are typically marginalized in retail databases.
Core Category Hierarchy
The following taxonomy outlines the recommended hierarchy for a digital wardrobe, prioritizing how users mentally group their items.
Level 1: Super-Category
Level 2: Core Category
Level 3: Functional Subcategory
Tops
T-Shirts & Jerseys
Short Sleeve, Long Sleeve, Sleeveless, Polo, Graphic Tee


Shirts & Blouses
Button-down, Blouse, Tunic, Flannel, Camisole


Sweaters & Knits
Crewneck, V-Neck, Turtleneck, Cardigan, Vest


Sweatshirts & Hoodies
Pullover Hoodie, Zip-up Hoodie, Crewneck Sweatshirt
Bottoms
Pants & Trousers
Tailored Trousers, Chinos, Cargo, Joggers, Leggings


Jeans
Straight, Skinny, Wide-Leg, Bootcut, Flared


Shorts
Denim Shorts, Athletic Shorts, Chino Shorts, Bermuda


Skirts
Mini, Midi, Maxi, Pencil, Pleated, Wrap
Dresses & Sets
Dresses
Casual Dress, Formal Gown, Sundress, Shirt Dress


One-Pieces
Jumpsuit, Romper, Overalls/Dungarees


Co-ord Sets
Suit (2-piece), Matching Loungewear Set, Tracksuit
Outerwear
Light Jackets
Denim Jacket, Leather Jacket, Bomber, Shacket, Blazer


Coats & Heavy Outerwear
Trench Coat, Wool Overcoat, Puffer Coat, Parka


Technical & Rainwear
Windbreaker, Raincoat, Ski/Snowboard Jacket
Footwear
Sneakers
Casual/Lifestyle Sneakers, Athletic/Running Shoes


Boots
Ankle Boots, Knee-High, Combat, Snow/Rain Boots


Formal & Dress Shoes
Oxfords, Loafers, Brogues, Derby, Monks


Heels & Pumps
Stiletto, Block Heel, Wedge, Kitten Heel


Sandals & Open Toe
Casual Sandals, Slides, Flip Flops, Espadrilles
Accessories
Bags & Luggage
Tote, Crossbody, Backpack, Clutch, Briefcase, Duffel


Headwear
Baseball Cap, Beanie, Sun Hat, Fedora, Beret


Neckwear & Belts
Scarf, Tie, Bowtie, Leather Belt, Fabric Belt, Harness


Eyewear & Jewelry
Sunglasses, Optical Glasses, Watch, Earrings, Necklace
Intimates & Swim
Underwear & Base Layers
Briefs, Bras, Thermal Base Layers, Bodysuits, Hosiery


Sleepwear & Loungewear
Pajama Sets, Robes, Sweatpants, Nightgowns


Swimwear
One-Piece, Bikini, Swim Trunks, Boardshorts, Rashguard
Cultural & Uniform
Traditional & Regional
Gomesi, Kanzu, Sari, Kurta, Kimono, Hanbok, Dirndl


Ceremonial & Occupational
Academic Regalia, Religious Vestments, Work Uniforms

Contextualizing Overlooked Categories
Western e-commerce taxonomies frequently relegate global garments to an "Other" category or force them into inaccurate structural buckets. For example, classifying a Ugandan Gomesi or Kanzu merely as a "Maxi Dress" or "Tunic" strips the garment of its cultural context, specific wearing rules, and formality status17. Allocating a dedicated top-level category for Traditional Wear ensures global relevance and allows users to tag appropriate ceremonial contexts, such as weddings or the Kwanjula19.
Similarly, the inclusion of loungewear and sleepwear is critical. With the global shift toward remote work environments, a significant portion of daily wear falls into this category. Capturing it allows the application to track true wardrobe utilization and cost-per-wear across the entire lifestyle, not just during public-facing moments21. Sets and layered items also require distinct architectural handling. The database must support parent-child relationships for sets, such as a two-piece suit. Users require the ability to log the item as a single entity but interact with its components independently for outfit shuffling.
Garment Identity and Visual Description
To support future virtual try-on (VTO) features, 3D avatar mapping, and advanced outfit generation, garments must be described by their structural identity. However, exposing a user to hundreds of fine-grained attributes during manual entry will cause immediate friction and platform abandonment5. The user-facing model must distill these into high-value, recognizable traits that directly impact how an item is worn and paired.
Foundational Visual Attributes
The most critical attributes for describing an item visually involve its spatial relationship to the human body. Silhouette and fit descriptors (e.g., Oversized, Relaxed, Regular, Slim, Skinny) are mandatory for outfit generation. An algorithm must be explicitly aware of volume to prevent illogical suggestions, such as attempting to layer a "Skinny" cardigan over an "Oversized" batwing blouse4.
Length serves as the next critical dimension. For tops, descriptors such as Cropped, Waist-length, Hip-length, and Tunic determine whether an item can be tucked in or worn with low-rise trousers. For bottoms, the spectrum from Micro to Floor-length dictates footwear compatibility. Neckline attributes (e.g., Crew, V-Neck, Scoop, Mock, Collared, Halter) dictate jewelry compatibility and vertical layering logic. Sleeve types govern horizontal layering; a dolman sleeve cannot comfortably fit beneath a slim-cut blazer.
Construction and Accessibility Details
Beyond basic shape, certain construction details heavily influence user behavior and must be captured. Waistband styles are particularly relevant to the lived reality of dressing. Differentiating between an elastic/drawstring waistband and a structured, tailored waistband with belt loops allows users to filter for comfort—a primary driver of daily wardrobe selection. Closure types (e.g., Pullover, Zip-front, Button-front, Wrap) assist in identifying items that may require specific styling, such as leaving a cardigan half-buttoned.
Distinguishing Static Identity from Styling States
A major architectural flaw in current wardrobe schemas is the conflation of a garment's physical construction with how it is styled on the body. Attributes such as "tucked," "rolled sleeves," "cuffed hems," or "half-zipped" are not inherent properties of the garment; they are temporal styling states22. The schema must allow an outfit record to override the base garment record. A "Long Sleeve Button-Down" remains static in the inventory database, but within the context of a specific outfit record, its state can be saved as sleeves_rolled and tucked_in. This distinction is foundational for generating realistic 3D avatars, as it informs the rendering engine how to simulate the fabric drape for that specific ensemble3.
Fabric, Feel, and Wear Behavior
Textile categorization in retail focuses almost exclusively on fiber composition to comply with international labeling regulations. However, end-users make dressing decisions based on perceived behavior—how a fabric feels, drapes, and reacts to the environment23. The database must separate the objective material composition from the subjective user experience.
Objective Composition vs. Subjective Behavior
Objective composition involves tracking the specific fibers: Cotton, Merino Wool, Cashmere, Silk, Linen, Polyester, Nylon, Rayon, Denim, or Leather. Tracking this objective data allows the application to calculate potential environmental impacts, microplastic shedding risks, and secondary market resale value25.
However, subjective wear behavior is mandatory for functional outfit generation. Two garments can be 100% polyester—one a sheer chiffon blouse, the other a dense fleece pullover. The algorithm must differentiate them based on user-perceived properties.

Behavioral Attribute
Options / Scales
Algorithmic & User Value
Thermal Weight
Sheer/Ultralight, Lightweight, Midweight, Heavyweight
Dictates seasonal appropriateness and insulation layering logic14.
Drape & Structure
Fluid/Drapey, Soft/Slouchy, Structured/Stiff
Informs the algorithm that a structured item can layer over a fluid item, but not vice versa24.
Stretch Profile
Rigid (No stretch), Comfort Stretch, Active Stretch
Dictates comfort levels and accommodation of bodily fluctuations.
Opacity
Sheer, Semi-Opaque, Fully Opaque
Prevents algorithms from suggesting a sheer top without a base layer underneath4.
Wrinkle Tendency
Wrinkle-Prone, Moderate, Wrinkle-Resistant
Highly valuable for users generating travel packing lists14.
Weather Suitability
Water-Resistant, Windproof, Quick-Dry, UPF-Rated
Crucial for technical outerwear and activewear categorization.

By tracking thermal weight and structure, an outfit generation algorithm can utilize semantic material weight. It can inherently deduce layering rules, eliminating the need for hard-coded, error-prone taxonomic restrictions16.
Care and Laundry Understanding
Proper garment maintenance extends the lifecycle of clothing and promotes sustainable consumption. Yet, the internationally standardized care symbols defined by ISO 3758 and ASTM D5489 remain esoteric to the average consumer28. A digital wardrobe app presents an opportunity to translate these hieroglyphs into an actionable, instantly understandable user interface.
Translating Care Symbols for the UI
The five base symbol categories—Washing, Bleaching, Drying, Ironing, and Professional Care—must be translated into plain-language dropdowns and intuitive icon-text pairings that users can parse without referencing an external guide28.
Care Category
Database Schema (Enum)
Plain-Language UI Translation
Simplified Iconography Concept
Washing
wash_machine_cold
Machine Wash Cold (30°C)
Tub filled with water + 1 Dot


wash_machine_warm
Machine Wash Warm (40°C)
Tub filled with water + 2 Dots


wash_hand
Hand Wash Only
Tub with a hand symbol


wash_do_not
Do Not Wash (Dry Clean)
Tub with a prominent 'X'
Drying
dry_tumble_low
Tumble Dry Low
Square containing a circle + 1 Dot


dry_tumble_normal
Tumble Dry Normal
Square containing a circle + 2 Dots


dry_air_hang
Hang to Dry
Square with a curved top line


dry_air_flat
Dry Flat (Reshape)
Square with a horizontal line inside
Ironing
iron_low
Iron Low (No Steam)
Iron silhouette + 1 Dot


iron_medium
Iron Medium
Iron silhouette + 2 Dots


iron_do_not
Do Not Iron
Iron silhouette with an 'X'
Bleaching
bleach_any
Any Bleach Allowed
Plain Triangle


bleach_non_chlorine
Non-Chlorine Bleach Only
Triangle with diagonal stripes


bleach_do_not
Do Not Bleach
Solid Triangle with an 'X'
Professional
clean_dry_only
Dry Clean Only
Circle with 'P' or 'F'


clean_do_not_dry
Do Not Dry Clean
Circle with an 'X'

Actionable Laundry Status Tracking
Beyond passive instructions, the application must actively track the laundry status of each garment. Integrating a temporal status field transforms the app from a static catalog into a dynamic daily utility11. The system should track statuses such as Clean_Ready (available for outfit generation), Worn_Reuse (worn, but suitable to wear again, which is crucial for preventing the accumulation of clothes on bedroom chairs), In_Hamper (unavailable, requiring home washing), and At_Cleaners (unavailable, currently off-site for professional care).
Color Naming and Color Semantics
Color matching is the most computationally intensive aspect of algorithmic styling, requiring an assessment of hue harmony, contrast, and seasonal palettes16. However, expecting a user to manually input HEX codes is prohibitive, and generating outfits based solely on broad text labels (e.g., "blue") results in clashing tones. The system must map complex technical color data to natural language semantics33.
The Translation Layer: Hex to Human
When a user uploads an image, the system's background removal and CV engine should extract the dominant HEX code. The application then passes this HEX through a color-distance algorithm against a predefined library of human-readable names35. While extensive databases of color names exist, displaying overly poetic names like "Silicon Valley Blue" or "Amethyst" confuses users during manual search33. The UI must restrict user-facing tags to a curated set of parent color families, while preserving the precise HEX code in the database for backend algorithmic use.
A practical color library should be based on foundational palette theory, separating colors into Base Neutrals, Main Colors, and Accents37. The recommended user-facing color families include Black, White, Grey/Silver, Beige/Cream, Brown/Camel, Navy, Blue, Light Blue, Red, Burgundy/Maroon, Pink/Blush, Green, Olive/Khaki, Yellow/Mustard, Orange/Rust, Purple, and Metallic/Gold.
Handling Multi-Tone, Gradients, and Washes
Solid colors are trivial to catalog, but textiles frequently feature complex treatments. The taxonomy must support a Primary Color (the background or dominant area) and an optional array of Secondary Colors.
Garments that have been subjected to heavy washing or fading (e.g., acid-wash denim, vintage tees) require special handling. A faded black shirt is functionally charcoal grey in the context of outfit matching. The database should include a Color Treatment modifier, with options such as Solid, Heathered/Melange, Faded/Washed, Gradient/Ombre, and Metallic Finish.
Pattern tags must remain entirely distinct from color tags. Recommended user-facing pattern attributes include Solid, Striped, Plaid/Tartan, Floral, Polka Dot, Animal Print, Geometric, and Graphic/Logo. By isolating the pattern from the color, a user can easily filter for a "Blue Floral Dress" without the system requiring a hard-coded "Blue Floral" combination tag40.
Brand, Provenance, and Authenticity
The source of a garment carries immense psychological weight. While tracking the brand name is standard industry practice, tracking the provenance—the origin story of the garment—supports the rising consumer trend of conscious consumerism, circular fashion, and emotional attachment to clothing2.
Distinguishing Retail Data
The database should allow users to distinguish between the overarching Brand (e.g., Ralph Lauren), the specific Sub-brand or Label (e.g., Polo Ralph Lauren vs. Ralph Lauren Purple Label), and the Manufacturer. For users engaging in personal logging, the Country of Origin (where the brand is based) and the Place of Manufacture (where the garment was physically sewn) provide insight into the quality and ethical footprint of the wardrobe.
Provenance and Authenticity Categories
Provenance defines how the user acquired the item. Categories should include Retail (New), Second-hand/Thrifted, Vintage, Gifted, Inherited, Handmade, Swapped, and Rented1. Recording items as "Thrifted" or "Handmade" provides users with analytics on their sustainable consumption habits, a major driver for modern wardrobe application adoption and user retention41.
For users cataloging high-value sneakers, luxury handbags, or archival streetwear, provenance borders on asset management. The schema must include an authenticity flag with options for Original/Verified, Replica/Dupe, or Unknown. Additionally, logging the condition at purchase (e.g., New with Tags, Excellent, Good, Distressed/Flawed) allows the system to accurately track the degradation of the item over its lifecycle.
Usage, Frequency, and Lifecycle
The core differentiator between a simple photo album and a wardrobe management system is the presence of usage analytics. Users desire visibility into what they actually wear, what they ignore, and whether their financial investments in clothing were justified1.
Wardrobe Maintenance and Rotation Metrics
The database must track several key lifecycle metrics to facilitate seasonal rotation and closet audits.
Wear Count: An integer that auto-increments every time the item is logged in a daily outfit.
Date Added: The date the item was registered in the application, used to calculate the frequency of use over time.
Last Worn Date: The most recent timestamp of use. This field is critical for surfacing "Neglected Items" to the user, prompting them to either wear the piece, repair it, or donate it44.
Cost-Per-Wear (CPW): A dynamically calculated field dividing the initial cost by the wear count. This metric heavily influences user psychology, turning fashion into a measurable investment and curbing impulse shopping27.
Item Completeness: A binary or relational field indicating if an item is missing a component. For instance, a two-piece suit missing the trousers, or a trench coat missing its matching tie-belt, is functionally incomplete and should be flagged for repair or replacement.
The Closet Audit Status
Wardrobes are living entities subject to seasonal audits and decluttering phases. Items should not merely be deleted from the database when they fall out of favor, as this irreversibly destroys historical outfit data and skews CPW analytics. Instead, they require a status lifecycle6. The Closet Status field should include options for Active (currently in rotation), Stored/Archived (boxed away for the off-season), Needs Repair (flagged for tailoring), Purgatory/Maybe (flagged for potential donation), and Retired/Donated (no longer owned, but historical data preserved).
Occasion, Climate, and Context
An offline-first system cannot rely entirely on live API weather feeds to dictate outfit suggestions, nor can it assume that "cold" means the same thing to a user in Scandinavia as it does to a user in Southeast Asia. Garments must be tagged with intrinsic contextual properties that users can filter manually based on their immediate reality.
Climate and Formality Tags
Rather than relying on strict meteorological seasons (Spring, Summer, Fall, Winter)—which fail entirely in equatorial climates or during international travel—garments should be tagged by thermal comfort zones9. Weather tags should include Freezing/Snow, Cold/Windy, Mild/Transitional, Warm/Sunny, Hot/Humid, and Rain-Resistant.
Users dress for specific destinations and social expectations. Contextual tags must reflect modern lifestyles6. Formality should scale from Active/Gym and Loungewear up through Casual, Smart Casual, Business Professional, and Formal/Black Tie. Social context tags should allow multi-selection, covering scenarios such as Office, Work-From-Home (which often prioritizes waist-up dressing), Date Night, Travel/Airport, Ceremony, and Nightclub. These tags directly influence outfit compatibility; an algorithm must recognize that a "Gym" top should not be paired with a "Business Professional" skirt unless specifically overridden by the user.
Missing Categories and Edge Cases
A rigid taxonomy will inevitably break when it encounters real-world fashion anomalies. The architecture must gracefully handle edge cases that traditional applications frequently overlook.
Layering Roles: An item must specify its structural role within an outfit. A Base Layer (must touch the skin, e.g., undershirt), a Mid Layer (worn over a base, e.g., a cardigan), or an Outer Shell (e.g., a raincoat). This explicit categorization is vital to prevent an outfit generator from suggesting a thick hoodie underneath a slim button-down shirt16.
Modular and Multi-Way Items: Garments such as reversible jackets, zip-off cargo pants, or multi-way wrap dresses defy simple categorization. The database should allow a Variant toggle, enabling the user to log the physical item once but switch its visual thumbnail, color properties, and length attributes depending on how it is currently configured.
Accessories Functioning as Clothing: Items like corsets, statement belts, suspenders, and harnesses often dictate the entire silhouette of an outfit. They must be available in the primary outfitting canvas alongside core garments, rather than relegated to a purely decorative list.
Field Model for Wardrobe Items
Based on the synthesis of the aforementioned research, the following database schema establishes a robust, highly normalized structure for wardrobe items. This model meticulously separates identity from physical state, allowing maximum flexibility for future technical enhancements.
1. Identity Fields

Field Name
Data Type
Purpose & Justification
item_id
UUID
Unique primary key identifying the garment49.
name
String
User-friendly designation (e.g., "Navy Work Chinos").
super_category
Enum
Top-level hierarchy (e.g., Bottoms).
core_category
Enum
Mid-level hierarchy (e.g., Pants & Trousers).
subcategory
Enum
Granular identification (e.g., Chinos).
variant_of
UUID (FK)
Links modular pieces to a parent item (e.g., zip-off shorts).

2. Visual Fields

Field Name
Data Type
Purpose & Justification
primary_color
Enum
Semantic color family for simple user filtering.
secondary_colors
Array[Enum]
Captures multi-tone garments.
color_hex
String
Extracted HEX code for backend AI color harmony35.
color_treatment
Enum
Washed, Faded, Heathered, Gradient, Metallic.
pattern
Enum
Solid, Floral, Stripe, Plaid, Graphic/Logo40.
image_url
String
Path to the local image with background removed15.

3. Material and Care Fields

Field Name
Data Type
Purpose & Justification
fiber_content
String
Objective material makeup (e.g., 100% Cotton).
thermal_weight
Enum
Sheer, Lightweight, Midweight, Heavyweight14.
structure
Enum
Fluid, Soft, Structured/Stiff (Informs drape algorithms)26.
stretch
Enum
Rigid, Comfort, Active (Informs comfort filtering).
wrinkle_tendency
Enum
Prone, Moderate, Resistant (Crucial for packing lists).
care_wash
Enum
Maps to simplified UI wash symbols (Machine, Hand, Do Not Wash).
care_dry
Enum
Maps to drying symbols (Tumble, Flat, Hang).

4. Brand and Provenance Fields

Field Name
Data Type
Purpose & Justification
brand
String
Primary label or designer.
provenance
Enum
Retail, Thrifted, Vintage, Handmade, Gifted1.
authenticity
Enum
Original, Replica, Unknown (For high-value tracking).
cost
Decimal
Original purchase price.
acquisition_date
Date
When the item was added to the wardrobe.

5. Usage and History Fields

Field Name
Data Type
Purpose & Justification
wear_count
Integer
Auto-increments when utilized in an outfit43.
last_worn
Date
Timestamp of most recent use to identify neglected items.
laundry_status
Enum
Clean_Ready, Worn_Reuse, In_Hamper, At_Cleaners.
closet_status
Enum
Active, Stored, Repair, Donate, Retired12.
completeness
Boolean
Flags if an item is missing a required component (e.g., a belt).

6. Fit and Construction Fields

Field Name
Data Type
Purpose & Justification
silhouette
Enum
Oversized, Relaxed, Slim, Skinny (Crucial for VTO collision).
length
Enum
Cropped, Waist, Hip, Knee, Ankle, Floor.
neckline
Enum
Crew, V-Neck, Collared, Halter, Turtleneck.
waistband
Enum
Elastic, Drawstring, Tailored/Rigid.
closure
Enum
Pullover, Zip-front, Button-front, Wrap.
layering_role
Enum
Base Layer, Mid Layer, Outer Shell16.

Recommended Filter Set for the App Interface
A comprehensive database is only functional if the interface can query it intuitively without overwhelming the user. The application interface should present a unified filtering drawer based on the most common psychological triggers for getting dressed.
Contextual Filters ("What am I dressing for?"): Quick toggles for Work, Casual, Night Out, or Gym.
Climate Filters ("What is the weather?"): Intuitive icons representing Sun, Clouds, Snow, or Rain.
Visual Filters ("What color do I want to wear?"): Interactive color swatches representing the user's base and accent palettes.
Utility Filters ("What is actually available?"): A vital toggle to "Hide Items in Laundry" or "Hide Seasonally Stored Items"11.
Analytic Filters ("What haven't I worn lately?"): Sort options for "Least Worn" or "Highest Cost-Per-Wear" to drive sustainable wardrobe rotation and combat the habit of reaching for the same five garments2.
Implementation Strategy: Minimal v1 vs. Expanded Schema
To prevent user burnout during the onboarding phase—where digitizing a 100-item wardrobe can take hours manually—the application must embrace progressive disclosure50.
Minimal First-Version Schema
The offline-first, manual-entry version should demand only the absolute minimum fields required to make the application useful immediately. The mandatory fields should be restricted to the Image (with local auto-background removal), the Super-Category, and the Primary Color. With just these three variables, a user can visualize their closet, build outfits on a two-dimensional canvas, and execute basic filtering. This provides the instant gratification necessary for user retention15.
Expanded Future Schema
Once the user is engaged, or when local, on-device AI vision models are introduced, the application can auto-populate the granular fields5. The AI can scan the image and silently populate subcategories, patterns, sleeve lengths, and exact HEX codes. Fields corresponding to cost, brand, and laundry care remain optional, filled out gradually as the user seeks deeper analytics—such as discovering their actual Cost-Per-Wear or organizing their laundry schedule46. This ensures the taxonomy stays flexible, adapting from a simple visual catalog into a highly technical, data-driven personal styling engine.
Works cited
10 Best Fashion Apps in 2026: From AI Stylists to Sustainable Second-Hand Marketplaces, https://www.thedroidsonroids.com/blog/10-best-fashion-apps
Wardrobe Management Apps and Their Unintended Benefits for Fashion Sustainability and Well-Being: Insights from User Reviews - MDPI, https://www.mdpi.com/2071-1050/17/9/4159
How to Build Digital Humans? From Priors to Photorealistic Avatars - Wojciech Zielonka, https://wojciechzielonka.com/how-to-build-digital-humans/
US9754410B2 - System and method for three-dimensional garment mesh deformation and layering for garment fit visualization - Google Patents, https://patents.google.com/patent/US9754410B2/en
Fashionpedia, https://fashionpedia.github.io/home/
Closet Audit Steps by a Fashion Wardrobe Stylist, https://www.elsabstyling.com/blog/step-by-step-closet-audit-fashion-wardrobe-stylist
Wardrobe Audit 101: The No-Nonsense Guide to a Closet Clean-Out - clothespetals.com, https://clothespetals.com/2025/08/02/wardrobe-audit-101-the-no-nonsense-guide-to-a-closet-clean-out/
Capsule Wardrobes: The Ultimate Guide - Oliver Charles, https://www.oliver-charles.com/pages/capsule-wardrobes-the-ultimate-guide
How to Build a Capsule Wardrobe: Steps & Tips | YAYA, https://yaya.eu/blogs/inspiration/how-to-build-a-capsule-wardrobe
how to build a capsule wardrobe the simple way (no planner needed) - alex p. hood, http://www.alexhood.co/blog/how-to-build-a-capsule-wardrobe-the-simple-way
Stylebook vs. Whering: Compare the Pros & Cons of All the Best Wardrobe Apps | Indyx, https://www.myindyx.com/versus/stylebook-vs-whering
The Wardrobe Audit: A Systematic Method for Evaluating What You Own an - selvane, https://www.selvane.co/blogs/knowledge/the-wardrobe-audit-a-systematic-method-for-evaluating-what-you-own-and-what-you-need
Libre Closet – open-source, self-hosted wardrobe organizer. Docker one-liner, SQLite, PWA. Looking for early feedback. - Reddit, https://www.reddit.com/r/selfhosted/comments/1rg99n7/libre_closet_opensource_selfhosted_wardrobe/
The Complete Guide to a Capsule Wardrobe: Definition & Tips - JudyP Apparel, https://judypapparel.com/blogs/judyp-fabrics/what-is-a-capsule-wardrobe
Best AI Outfit Planner for Android & iOS: Top Apps Compared (2026), https://selionai.app/blog/best-ai-outfit-planner-android-ios
Loom: Hybrid Retrieval-Scoring Outfit Recommendation with Semantic Material Compatibility and Occasion-Aware Embedding Priors - arXiv, https://arxiv.org/html/2605.09830v1
Gomesi - Wikipedia, https://en.wikipedia.org/wiki/Gomesi
Complete Guide to Uganda's Clothing - The Fashiongton Post, https://fashiongtonpost.com/uganda-traditional-clothing/
Uganda's Cultural Dress: From Gomesi to Kanzu, https://theugandablog.com/blog/culture/ugandas-cultural-dress/
Kanzu - Wikipedia, https://en.wikipedia.org/wiki/Kanzu
Capsule Wardrobes 101: Build a Closet That Works for Every Season - Jo MacIntosh, https://www.jomacintosh.com/the-blog/Capsule-Wardrobes-101
MV-Fashion: Towards Enabling Virtual Try-On and Size Estimation with Multi-View Paired Data - arXiv, https://arxiv.org/html/2603.08147v1
GLCM texture based fractal method for evaluating fabric surface roughness - ResearchGate, https://www.researchgate.net/publication/224758823_GLCM_texture_based_fractal_method_for_evaluating_fabric_surface_roughness
APPLICATION OF FUZZY LOGIC IN WEAVING PROCESS: A SYSTEMATIC LITERATURE REVIEW, http://www.jatit.org/volumes/Vol101No23/40Vol101No23.pdf
A Hybrid Artificial Intelligence Framework for Mobile Digital Wardrobes Supporting Sustainable Fashion Choices and Personalized Outfit Generation - ResearchGate, https://www.researchgate.net/publication/405477660_A_Hybrid_Artificial_Intelligence_Framework_for_Mobile_Digital_Wardrobes_Supporting_Sustainable_Fashion_Choices_and_Personalized_Outfit_Generation
A Sustainable Framework for Realism Evaluation and Optimization of Virtual Fabric Drape Effect - MDPI, https://www.mdpi.com/2071-1050/17/12/5550
Whering: Your Digital Wardrobe – Apps on Google Play, https://play.google.com/store/apps/details/Whering_Digital_Closet_Stylist?id=com.whering.app&hl=en_NZ
Laundry symbol - Wikipedia, https://en.wikipedia.org/wiki/Laundry_symbol
V8 - Technical Booklet | PDF | Chemistry | Materials - Scribd, https://www.scribd.com/document/475557214/V8-Technical-Booklet
Fabric Care Labels & Laundry Washing Symbols (& All The Meanings) - SewGuide, https://sewguide.com/fabric-care-symbols/
Textile Care Symbols Guide | PDF | Laundry | Manufactured Goods - Scribd, https://fr.scribd.com/doc/47097088/acsguide-050608
Best Wardrobe Apps in 2026: 10 Closet Apps Tested & Ranked - Nouva, https://www.nouva.app/blog/best-wardrobe-apps-2026-comparison
hex_to_color Convert Hex Codes to Color Names - RDocumentation, https://www.rdocumentation.org/packages/col2hex2col/versions/0.5.3/topics/hex_to_color
hex-color-to-color-name - NPM, https://www.npmjs.com/package/hex-color-to-color-name
Color Converter — HEX, RGB, CMYK & Pantone TCX - SDF Clothing, https://sdfltd.com/tools/color-converter/
Color Name Identifier Online | Find HEX, Pantone & RAL Name | PaletaColor Pro, https://paletacolorpro.com/en/color-name-finder
Create Your Capsule Wardrobe Color Palette: A Guide - Rue Sophie, https://ruesophie.com/blogs/the-style-edit/capsule-wardrobe-color-palette
Step 4: Create A Colour Palette | the concept wardrobe, https://theconceptwardrobe.com/build-a-wardrobe/step-4-create-a-colour-palette
135+ Different Types of Color Palette for Women's Clothing (With Pictures) - LoopedInLooks, https://loopedinlooks.com/different-types-of-color-palette-for-women-clothing/
Types of Fabric Patterns: Identifying Common Fabric Patterns with Sinocomfort - Comfort, https://sinocomfort.com/blog/types-of-fabric-patterns/
Wardrobe Management App Market Research Report 2034 - Market Intelo, https://marketintelo.com/report/wardrobe-management-app-market
(PDF) Wardrobe Management Apps and Their Unintended Benefits for Fashion Sustainability and Well-Being: Insights from User Reviews - ResearchGate, https://www.researchgate.net/publication/391446595_Wardrobe_Management_Apps_and_Their_Unintended_Benefits_for_Fashion_Sustainability_and_Well-Being_Insights_from_User_Reviews
Airtable Wardrobe Tracker - M Gets Dressed, https://www.mgetsdressed.com/wardrobe-tracking
5 Best Digital Wardrobe Apps of 2026 (Ranked & Tested) - selion.ai, https://selionai.app/blog/digital-wardrobe-app
Virtual Closet Assistant - International Journal of Innovative Research in Science, https://www.ijirset.com/upload/2025/october/27_Virtual%20Closet%20Assistant%20An%20AI-Driven%20Approach%20for%20Intelligent%20Wardrobe%20Management%20and%20Personalized%20Outfit%20Recommendation.pdf
Cost per Wear Tracker | Outfit Planner | Closet Tracker | Closet Spreadsheet Template - Etsy, https://www.etsy.com/listing/1490868523/cost-per-wear-tracker-outfit-planner
Capsule Wardrobe Preparation Ideas and Tips - Doğtaş, https://www.dogtas.com/en/capsule-wardrobe-preparation-ideas-and-tips
How To Start A Capsule Wardrobe: A Guide for Beginners - Pinch of Yum, https://pinchofyum.com/how-to-start-a-capsule-wardrobe
Digital Wardrobe And Outfit Planner Database, https://databasesample.com/database/digital-wardrobe-and-outfit-planner-database
Stylebook vs Cladwell vs Whering vs PutTogether: 2026 Wardrobe App Showdown, https://www.puttogether.world/guides/digital-closet/stylebook-vs-cladwell-vs-whering-vs-puttogether-2026
Virtual Wardrobe Planner Overview | PDF | Clothing | Databases - Scribd, https://www.scribd.com/document/931205159/Research-Paper
