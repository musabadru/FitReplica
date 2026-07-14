package com.fitreplica.core.model

data class WearHistoryEntry(
    val id: WearEventId,
    val itemId: ClothingId,
    val itemName: String,
    val itemType: ClothingType,
    val colorPrimary: String,
    val outfitId: OutfitId?,
    val wornAt: Long,
    val context: String?,
    val notes: String?,
)
