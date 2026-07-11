# Canonical Schema v2

## 06 - Final Schema: v2

Goal: extend v1 for import, OCR, analytics, provenance, detailed care, and avatar-ready metadata while preserving v1 records.

## Expanded Groups

| Group | Purpose |
|---|---|
| `identity` | Stable taxonomy, aliases, owner profile, set relationships. |
| `visual` | Exact colour values, pattern, finish, photo roles, avatar image hints. |
| `materials` | Fibre composition and behavior properties. |
| `care` | Structured care instructions for washing, bleaching, drying, ironing, and professional care. |
| `brand` | Brand, label, manufacturer, and origin fields. |
| `provenance` | Acquisition, authenticity, price, source, and ownership history. |
| `usage` | Laundry state, wear history, cost per wear, tags, and compatibility. |
| `fit` | Fit, silhouette, size, lengths, and body-zone attributes. |
| `construction` | Closure, collar, pockets, seams, embellishments, heel and boot fields. |
| `relationships` | Sets, pairs, capsules, outfit templates, and variants. |
| `externalMappings` | GTIN, SKU, Google, Shopify, Schema.org, source payloads, import confidence. |
| `status` | Condition, repair, archive, retired, and deletion lifecycle. |

## JSON Sketch

```json
{
  "id": "uuid",
  "taxonomyVersion": "wardrobe-taxonomy-1",
  "itemKind": "garment",
  "categoryPath": ["garments", "tops", "shirts", "button_down_shirt"],
  "name": "Blue striped office shirt",
  "aliases": ["work shirt"],
  "ownerProfileId": null,
  "photos": [
    {
      "id": "photo_1",
      "role": "front",
      "localUri": "content://local/photo_1",
      "backgroundRemoved": true
    }
  ],

  "visual": {
    "dominantHex": "#4E6FAE",
    "dominantOklch": { "l": 0.62, "c": 0.09, "h": 255.0 },
    "semanticColourId": "blue",
    "secondaryColourIds": ["white"],
    "baseColourId": "blue",
    "colourDetailName": "medium blue",
    "colourTreatment": "solid",
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
      "thermalWeight": "lightweight",
      "drape": "structured",
      "stretchLevel": "low",
      "opacity": "opaque",
      "breathability": "medium",
      "insulation": "low",
      "wrinkleResistance": "medium",
      "waterResistance": "none",
      "delicateCare": false
    }
  },

  "care": {
    "summary": "wash_30_line_dry_low_iron",
    "washing": {
      "allowed": true,
      "method": "machine",
      "maxTempC": 30,
      "cycle": "mild"
    },
    "bleaching": {
      "allowed": false,
      "bleachType": "none"
    },
    "drying": {
      "tumbleAllowed": false,
      "tumbleHeat": "none",
      "naturalDryMethod": "line",
      "shade": false
    },
    "ironing": {
      "allowed": true,
      "heatLevel": 2,
      "maxTempC": 150,
      "steamAllowed": true
    },
    "professionalCare": {
      "allowed": false,
      "method": "none",
      "solventCode": null,
      "process": null
    }
  },

  "brand": {
    "brandName": "Example Brand",
    "subBrand": null,
    "labelName": null,
    "manufacturerName": "Example Manufacturing Ltd",
    "countryOfOrigin": "TR",
    "placeOfManufacture": "Izmir, Turkey"
  },

  "provenance": {
    "authenticityStatus": "original",
    "acquisitionType": "bought_new",
    "acquisitionSource": "retail",
    "purchaseDate": null,
    "purchasePrice": 32.0,
    "purchaseCurrency": "GBP",
    "giftedBy": null,
    "conditionAtAcquisition": "new"
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
    "costPerWear": null,
    "compatibilityTags": ["office", "layering"]
  },

  "fit": {
    "sizeLabel": "M",
    "sizeSystem": "alpha",
    "fitType": "regular",
    "silhouette": "straight",
    "sleeveLength": "long",
    "neckline": "collared",
    "topLength": "regular",
    "layeringRole": "mid_layer",
    "layeringIndex": 1
  },

  "construction": {
    "collarType": "button_down",
    "closureType": "buttoned",
    "pocketType": "chest",
    "waistbandType": null,
    "rise": null,
    "legShape": null,
    "hemLength": null,
    "heelHeight": null,
    "bootShaftHeight": null,
    "seamDetails": ["double_stitched"],
    "embellishments": []
  },

  "relationships": {
    "setMembership": null,
    "parentItemId": null,
    "variantOfItemId": null,
    "pairedItemIds": [],
    "capsuleIds": [],
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
    "closetStatus": "active",
    "repairStatus": "none",
    "isFavourite": false,
    "isArchived": false,
    "deletedAt": null
  }
}
```

## Advanced Filter Candidates

- Material composition
- Material behavior: thermal weight, stretch, drape, opacity, wrinkle tendency, water resistance
- Pattern and surface finish
- Fit, silhouette, length, neckline, sleeve length
- Closure, collar, waistband, pockets, heel height, boot shaft height
- Brand, sub-brand, manufacturer, country of origin
- Acquisition type, authenticity, purchase source
- Cost per wear, rarely worn, never worn, most worn
- Storage location, capsule, set membership
- Import confidence and external source

## Data Modeling Rules

- Keep v2 groups optional.
- Use related tables for many-to-many tags, sets, capsules, outfits, and wear events.
- Keep exact external taxonomy IDs in `externalMappings`; never use them as FitReplica's source of truth.
- Keep outfit-specific styling overrides on outfit records, not item records.
- Keep UI labels localizable and separate from enum IDs.

