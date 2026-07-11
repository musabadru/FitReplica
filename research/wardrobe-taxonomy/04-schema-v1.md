# Canonical Schema v1

## 06 - Final Schema: v1

Goal: support fast manual entry and useful offline filtering without forcing advanced research fields into the first capture flow.

## Required Capture Flow

The first add-item flow should be able to save an item with:

- Photo or image reference
- Primary physical category
- Item type
- Dominant semantic colour
- Laundry status
- Condition status

Everything else in v1 may be optional or suggested after save.

## Item Fields

| Field | Type | Required | Notes |
|---|---|---:|---|
| `id` | UUID/string | Yes | Stable local identity. |
| `taxonomyVersion` | String | Yes | Example: `wardrobe-taxonomy-1`. |
| `itemKind` | Enum | Yes | `garment`, `footwear`, `bag`, `accessory`. |
| `primaryCategoryId` | String | Yes | Physical family, such as `tops` or `boots`. |
| `subcategoryId` | String | Yes | Specific physical type, such as `button_down_shirt`. |
| `name` | String | No | User-facing label. Can be generated from type and colour. |
| `photoIds` | List<String> | Yes | At least one image should be preferred, but import can allow placeholder records. |
| `dominantColourId` | Enum/String | Yes | Semantic palette value. |
| `secondaryColourIds` | List<Enum/String> | No | For stripes, trim, or multi-tone items. |
| `patternType` | Enum | No | Defaults to `solid` if unknown. |
| `materialSummary` | String | No | Coarse text or enum, such as `cotton_blend`. |
| `seasonTags` | List<String> | No | `summer`, `winter`, `rainy`, `transitional`. |
| `weatherTags` | List<String> | No | `hot`, `cold`, `rainy`, `humid`, `dry`, `indoor`, `outdoor`. |
| `occasionTags` | List<String> | No | `work`, `casual`, `formal`, `gym`, `travel`, etc. |
| `laundryStatus` | Enum | Yes | See v1 enum below. |
| `conditionStatus` | Enum | Yes | See v1 enum below. |
| `fitType` | Enum | No | Useful but should not block save. |
| `brandName` | String | No | Optional manual entry. |
| `authenticityStatus` | Enum | No | Optional in v1. |
| `lastWornAt` | DateTime/null | No | Null until first wear event. |
| `wearCount` | Integer | Yes | Defaults to 0. |
| `isFavourite` | Boolean | Yes | Defaults to false. |
| `isArchived` | Boolean | Yes | Defaults to false. |
| `notes` | String | No | Freeform user notes. |

## v1 Category Shape

Use stable IDs, not display strings.

| Root | Family examples |
|---|---|
| `garment` | `tops`, `bottoms`, `one_piece`, `tailoring`, `outerwear`, `underwear_base_layers`, `sleep_lounge`, `active_swim`, `work_uniform`, `traditional_ceremonial` |
| `footwear` | `casual_dress_shoes`, `boots`, `sandals`, `slippers`, `athletic_shoes` |
| `bag` | `backpack`, `tote`, `handbag`, `shoulder_bag`, `crossbody`, `clutch`, `briefcase`, `duffel` |
| `accessory` | `belt`, `scarf`, `hat`, `cap`, `gloves`, `tie`, `jewellery`, `eyewear`, `hosiery_small_goods` |

## v1 Enums

### `laundryStatus`

- `clean`
- `worn_reusable`
- `dirty`
- `in_laundry`
- `at_cleaners`

### `conditionStatus`

- `new`
- `good`
- `worn`
- `needs_repair`
- `torn`
- `retired`

### `authenticityStatus`

- `original`
- `replica`
- `thrifted`
- `gifted`
- `handmade`
- `unknown`

### `patternType`

- `solid`
- `striped`
- `plaid`
- `checked`
- `floral`
- `geometric`
- `polka_dot`
- `camo`
- `animal_print`
- `abstract`
- `graphic`
- `textured`
- `other`

### `fitType`

- `slim`
- `regular`
- `relaxed`
- `oversized`
- `fitted`
- `skinny`
- `loose`
- `unknown`

## v1 Quick Filters

- Category
- Colour
- Laundry status
- Condition
- Occasion
- Season/weather
- Last worn
- Wear count
- Favourite

## v1 JSON Sketch

```json
{
  "id": "uuid",
  "taxonomyVersion": "wardrobe-taxonomy-1",
  "itemKind": "garment",
  "primaryCategoryId": "tops",
  "subcategoryId": "button_down_shirt",
  "name": "Blue striped office shirt",
  "photoIds": ["photo_1"],
  "dominantColourId": "blue",
  "secondaryColourIds": ["white"],
  "patternType": "striped",
  "materialSummary": "cotton_blend",
  "seasonTags": ["transitional"],
  "weatherTags": ["indoor", "dry"],
  "occasionTags": ["work", "casual"],
  "laundryStatus": "clean",
  "conditionStatus": "good",
  "fitType": "regular",
  "brandName": null,
  "authenticityStatus": "unknown",
  "lastWornAt": null,
  "wearCount": 0,
  "isFavourite": false,
  "isArchived": false,
  "notes": null
}
```

## Implementation Notes

- Keep display labels separate from stored IDs.
- Keep item category stable even when usage changes.
- Treat `wearCount` as denormalized from wear events once wear logging exists.
- Do not require material, care, brand, price, country of origin, or construction details in first-run item capture.

